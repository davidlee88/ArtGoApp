/*
 * Copyright (c) 2013 Akexorcist
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ArtGo.ArtGoApp.libs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.ArtGo.ArtGoApp.utils.MySingleton;
import com.ArtGo.ArtGoApp.utils.Utils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

@SuppressLint("NewApi")
public class GoogleDirection {
    public final static String MODE_DRIVING = "driving";
    public final static String STATUS_ZERO_RESULTS = "ZERO_RESULTS";

    private OnDirectionResponseListener mDirectionListener = null;
    private OnAnimateListener mAnimateListener = null;

    private boolean isLogging = false;

    private LatLng animateMarkerPosition = null;
    private LatLng beginPosition = null;
    private LatLng endPosition = null;
    private ArrayList<LatLng> animatePositionList = null;
    private Marker animateMarker = null;
    private Polyline animateLine = null;
    private GoogleMap gm = null;
    private int step = -1;
    private double totalAnimateDistance = 0;
    private boolean isAnimated = false;

    private Context mContext = null;

    public GoogleDirection(Context context) {
        mContext = context;
    }

    public String request(LatLng start, LatLng end, String mode) {
        final String url = "http://maps.googleapis.com/maps/api/directions/xml?"
                + "origin=" + start.latitude + "," + start.longitude
                + "&destination=" + end.latitude + "," + end.longitude
                + "&sensor=false&units=metric&mode=" + mode;

        if(isLogging)
            Log.i("GoogleDirection", "URL : " + url);
        getDirection(url);

        return url;
    }

    private void getDirection(String url){
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        Document d1 = null;
                        //if(mDirectionListener != null)
                        //String xmlRecords = "<data><terminal_id>1000099999</terminal_id><merchant_id>10004444</merchant_id><merchant_info>Mc Donald's - Abdoun</merchant_info></data>";

                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = null;
                        try {
                            builder = factory.newDocumentBuilder();
                        } catch (ParserConfigurationException e) {
                            e.printStackTrace();
                        }
                        try {
                            d1 = builder.parse(new InputSource(new StringReader(response)));
                        } catch (SAXException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mDirectionListener.onResponse(getStatus(d1), d1, GoogleDirection.this);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // handle error response
                    }
                }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(Utils.ARG_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        MySingleton.getInstance(mContext).getRequestQueue().add(request);
    }

    public String getStatus(Document doc) {
        NodeList nl1 = doc.getElementsByTagName("status");
        Node node1 = nl1.item(0);
        if(isLogging)
            Log.i("GoogleDirection", "Status : " + node1.getTextContent());
        return node1.getTextContent();
    }

    public ArrayList<LatLng> getDirection(Document doc) {
        NodeList nl1, nl2, nl3;
        ArrayList<LatLng> listGeopoints = new ArrayList<>();
        nl1 = doc.getElementsByTagName("step");
        if (nl1.getLength() > 0) {
            for (int i = 0; i < nl1.getLength(); i++) {
                Node node1 = nl1.item(i);
                nl2 = node1.getChildNodes();

                Node locationNode = nl2.item(getNodeIndex(nl2, "start_location"));
                nl3 = locationNode.getChildNodes();
                Node latNode = nl3.item(getNodeIndex(nl3, "lat"));
                double lat = Double.parseDouble(latNode.getTextContent());
                Node lngNode = nl3.item(getNodeIndex(nl3, "lng"));
                double lng = Double.parseDouble(lngNode.getTextContent());
                listGeopoints.add(new LatLng(lat, lng));

                locationNode = nl2.item(getNodeIndex(nl2, "polyline"));
                nl3 = locationNode.getChildNodes();
                latNode = nl3.item(getNodeIndex(nl3, "points"));
                ArrayList<LatLng> arr = decodePoly(latNode.getTextContent());
                for(int j = 0 ; j < arr.size() ; j++) {
                    listGeopoints.add(new LatLng(arr.get(j).latitude
                            , arr.get(j).longitude));
                }

                locationNode = nl2.item(getNodeIndex(nl2, "end_location"));
                nl3 = locationNode.getChildNodes();
                latNode = nl3.item(getNodeIndex(nl3, "lat"));
                lat = Double.parseDouble(latNode.getTextContent());
                lngNode = nl3.item(getNodeIndex(nl3, "lng"));
                lng = Double.parseDouble(lngNode.getTextContent());
                listGeopoints.add(new LatLng(lat, lng));
            }
        }

        return listGeopoints;
    }

    public PolylineOptions getPolyline(Document doc, int width, int color) {
        ArrayList<LatLng> arr_pos = getDirection(doc);
        PolylineOptions rectLine = new PolylineOptions().width(dpToPx(width)).color(color);
        for(int i = 0 ; i < arr_pos.size() ; i++)
            rectLine.add(arr_pos.get(i));
        return rectLine;
    }

    private int getNodeIndex(NodeList nl, String nodename) {
        for(int i = 0 ; i < nl.getLength() ; i++) {
            if(nl.item(i).getNodeName().equals(nodename))
                return i;
        }
        return -1;
    }

    private ArrayList<LatLng> decodePoly(String encoded) {
        ArrayList<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng position = new LatLng((double)lat / 1E5, (double)lng / 1E5);
            poly.add(position);
        }
        return poly;
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public void setOnDirectionResponseListener(OnDirectionResponseListener listener) {
        mDirectionListener = listener;
    }

    public interface OnDirectionResponseListener {
        void onResponse(String status, Document doc, GoogleDirection gd);
    }

    public interface OnAnimateListener {
        void onFinish();
        void onStart();
        void onProgress(int progress, int total);
    }

    public void cancelAnimated() {
        isAnimated = false;
    }

    private Runnable r = new Runnable() {
        public void run() {

            animateMarkerPosition = getNewPosition(animateMarkerPosition, endPosition);

            boolean drawMarker = false;
            if(drawMarker)
                animateMarker.setPosition(animateMarkerPosition);


            boolean drawLine = false;
            if(drawLine) {
                List<LatLng> points = animateLine.getPoints();
                points.add(animateMarkerPosition);
                animateLine.setPoints(points);
            }

            if((animateMarkerPosition.latitude == endPosition.latitude
                    && animateMarkerPosition.longitude == endPosition.longitude)) {
                if(step == animatePositionList.size() - 2) {
                    isAnimated = false;
                    totalAnimateDistance = 0;
                    if(mAnimateListener != null)
                        mAnimateListener.onFinish();
                } else {
                    step++;
                    beginPosition = animatePositionList.get(step);
                    endPosition = animatePositionList.get(step + 1);
                    animateMarkerPosition = beginPosition;

                    boolean flatMarker = false;
                    if(flatMarker && step + 3 < animatePositionList.size() - 1) {
                        float rotation = getBearing(animateMarkerPosition, animatePositionList.get(step + 3)) + 180;
                        animateMarker.setRotation(rotation);
                    }

                    if(mAnimateListener != null)
                        mAnimateListener.onProgress(step, animatePositionList.size());
                }
            }

            boolean cameraLock = false;
            double animateCamera = -1;
            if(cameraLock && (totalAnimateDistance > animateCamera || !isAnimated)) {
                totalAnimateDistance = 0;
                float bearing = getBearing(beginPosition, endPosition);
                CameraPosition.Builder cameraBuilder = new CameraPosition.Builder()
                        .target(animateMarkerPosition).bearing(bearing);

                boolean isCameraTilt = false;
                if(isCameraTilt)
                    cameraBuilder.tilt(90);
                else
                    cameraBuilder.tilt(gm.getCameraPosition().tilt);

                boolean isCameraZoom = false;
                int zoom = -1;
                if(isCameraZoom)
                    cameraBuilder.zoom(zoom);
                else
                    cameraBuilder.zoom(gm.getCameraPosition().zoom);

                CameraPosition cameraPosition = cameraBuilder.build();
                gm.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }

            if(isAnimated) {
                int animateSpeed = -1;
                new Handler().postDelayed(r, animateSpeed);
            }
        }
    };

    private LatLng getNewPosition(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        double dis = Math.sqrt(Math.pow(lat, 2) + Math.pow(lng, 2));
        double animateDistance = -1;
        if(dis >= animateDistance) {
            double angle = -1;

            if(begin.latitude <= end.latitude && begin.longitude <= end.longitude)
                angle = Math.toDegrees(Math.atan(lng / lat));
            else if(begin.latitude > end.latitude && begin.longitude <= end.longitude)
                angle = (90 - Math.toDegrees(Math.atan(lng / lat))) + 90;
            else if(begin.latitude > end.latitude && begin.longitude > end.longitude)
                angle = Math.toDegrees(Math.atan(lng / lat)) + 180;
            else if(begin.latitude <= end.latitude && begin.longitude > end.longitude)
                angle = (90 - Math.toDegrees(Math.atan(lng / lat))) + 270;

            double x = Math.cos(Math.toRadians(angle)) * animateDistance;
            double y = Math.sin(Math.toRadians(angle)) * animateDistance;
            totalAnimateDistance += animateDistance;
            double finalLat = begin.latitude + x;
            double finalLng = begin.longitude + y;

            return new LatLng(finalLat, finalLng);
        } else {
            return end;
        }
    }

    private float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);
        if(begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float)(Math.toDegrees(Math.atan(lng / lat)));
        else if(begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float)((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if(begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return  (float)(Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if(begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float)((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }
}
