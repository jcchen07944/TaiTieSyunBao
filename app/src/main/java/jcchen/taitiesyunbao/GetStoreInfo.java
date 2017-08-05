package jcchen.taitiesyunbao;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Vector;

import static jcchen.taitiesyunbao.Constant.LOADING_HANDLER_END;


/**
 * Created by JCChen on 2017/7/15.
 */
public class GetStoreInfo extends AsyncTask<Void, Void, StoreInfo> {

    private RecyclerView.Adapter adapter;

    private Vector<StoreInfo> storeList;

    private Handler handler;

    private StoreInfo storeInfo;

    private boolean isLast;


    public GetStoreInfo(RecyclerView.Adapter adapter, Vector<StoreInfo> storeList, StoreInfo storeInfo, Handler handler, boolean isLast) {
        this.adapter = adapter;
        this.storeList = storeList;
        this.handler = handler;
        this.storeInfo = storeInfo;
        this.isLast = isLast;
    }


    protected StoreInfo doInBackground(Void... params) {
        String ID = storeInfo.getID();
        String Latitude = storeInfo.getLatitude();
        String Longitude = storeInfo.getLongitude();

        JSONObject cacheResponse = null;
        try {
            URL url = new URL("https://www.google.com/maps/search/?api=1&query=" +
                    Latitude + "," + Longitude + "&query_place_id=" + ID + "&hl=zh-TW");
            cacheResponse = getCacheResponse(url);
            storeInfo.setName(cacheResponse.getJSONArray("j").getJSONArray(8).getString(1));
            storeInfo.setTel(cacheResponse.getJSONArray("j").getJSONArray(8).getString(7));
            try {
                if (!storeInfo.getTel().equals("null")) {
                    String[] token = storeInfo.getTel().split(" ");
                    storeInfo.setTel("(0" + token[1] + ") " + token[2] + " " + token[3]);
                }
            }
            catch (Exception e) {  }
            storeInfo.setRate(cacheResponse.getJSONArray("j").getJSONArray(8).getString(3));
            storeInfo.set_storeID(cacheResponse.getJSONArray("j").getJSONArray(8).getJSONArray(0).getString(0));
            storeInfo.setImage(getImageUrl(storeInfo.get_storeID()));
            String ReviewAmount = cacheResponse.getJSONArray("j").getJSONArray(8).getString(4);
            if(!ReviewAmount.equals("null"))
                storeInfo.setReviewAmount(Integer.valueOf(ReviewAmount.split(" ")[0]));
            else
                storeInfo.setReviewAmount(0);
            return storeInfo;
        } catch (Exception e) {
            return null;
        }
    }

    protected void onPostExecute(StoreInfo storeInfo) {
        Message msg = new Message();
        msg.what = LOADING_HANDLER_END;
        storeList.add(storeInfo);
        adapter.notifyItemChanged(storeList.size() - 1);
        if(isLast)
            handler.sendMessage(msg);
    }

    private Vector<ImageAttr> getImageUrl(String storeID) {
        Vector<ImageAttr> Image = new Vector<>();
        String response, jsonData = null;
        BufferedReader reader = null;

        try {
            storeID = URLEncoder.encode(storeID, "UTF-8");
            URL url = new URL("https://www.google.com/maps/uv?pb=!1s" + storeID + "&hl=zh-Hant");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty( "User-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.4 (KHTML, like Gecko) Chrome/22.0.1229.94 Safari/537.4" );
            connection.setInstanceFollowRedirects(true);
            connection.setReadTimeout(10 * 1000);
            connection.connect();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
            response = stringBuilder.toString();
            int index = response.indexOf("window.APP_OPTIONS=") + 19;
            jsonData = "{\"j\":" +
                    response.substring(index, response.indexOf(";window.JS_VERSION=")) +
                    "}";
            JSONObject jsonObject = new JSONObject(jsonData);
            int len = jsonObject.getJSONArray("j").getJSONArray(7).getJSONArray(0).length();
            for(int i = 0; i < len; i++) {
                String imageID = jsonObject.getJSONArray("j").getJSONArray(7)
                                        .getJSONArray(0).getJSONArray(i).getString(0);
                ImageAttr imageAttr = new ImageAttr();
                if(!jsonObject.getJSONArray("j").getJSONArray(7).getJSONArray(0).getJSONArray(i).getJSONArray(6).getString(0).startsWith("//geo")) {
                    imageAttr.setImageUrl("http://lh6.googleusercontent.com/" + imageID + "/");
                    try {
                        imageAttr.setProvider(jsonObject.getJSONArray("j").getJSONArray(7).getJSONArray(0)
                                .getJSONArray(i).getJSONArray(18)
                                .getJSONArray(0).getJSONArray(1)
                                .getString(1));
                        imageAttr.setOriginalSite(jsonObject.getJSONArray("j").getJSONArray(7)
                                .getJSONArray(0).getJSONArray(i).getJSONArray(18)
                                .getJSONArray(0).getJSONArray(1)
                                .getString(0));
                    } catch (Exception e) {
                        imageAttr.setProvider("null");
                        imageAttr.setOriginalSite("null");
                    }
                    Image.add(imageAttr);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return Image;
        }
    }

    private JSONObject getCacheResponse(final URL url) {
        String response, jsonData = null;
        BufferedReader reader = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty( "User-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.4 (KHTML, like Gecko) Chrome/22.0.1229.94 Safari/537.4" );
            connection.setInstanceFollowRedirects(true);
            connection.setReadTimeout(10 * 1000);
            connection.connect();

            reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
            response = stringBuilder.toString();
            int index = response.indexOf("cacheResponse",
                    response.indexOf("cacheResponse") + 1);
            jsonData = "{\"j\":" +
                    response.substring(index + 14, response.indexOf("]]);", index) + 2) +
                    "}";
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            return new JSONObject(jsonData);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
