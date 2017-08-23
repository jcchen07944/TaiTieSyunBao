package jcchen.taitiesyunbao.Model;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import jcchen.taitiesyunbao.ImageAttr;
import jcchen.taitiesyunbao.Presenter.OnStoreListener;
import jcchen.taitiesyunbao.StoreInfo;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by JCChen on 2017/8/24.
 */

public class GetStoreInfoV2 {

    private StoreInfo storeInfo;
    private OnStoreListener onStoreListener;

    public GetStoreInfoV2(StoreInfo storeInfo, OnStoreListener onStoreListener) {
        this.storeInfo = storeInfo;
        this.onStoreListener = onStoreListener;

        String ID = storeInfo.getID();
        String Latitude = storeInfo.getLatitude();
        String Longitude = storeInfo.getLongitude();
        try {
            getInfo(new URL("https://www.google.com/maps/search/?api=1&query=" +
                    Latitude + "," + Longitude + "&query_place_id=" + ID + "&hl=zh-TW"));
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private void getInfo(final URL url) {
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .addHeader("User-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.4 (KHTML, like Gecko) Chrome/22.0.1229.94 Safari/537.4")
                .url(url)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonData = response.body().string();
                int index = jsonData.indexOf("cacheResponse",
                        jsonData.indexOf("cacheResponse") + 1);
                jsonData = "{\"j\":" + jsonData.substring(index + 14, jsonData.indexOf("]]);", index) + 2) + "}";

                try {
                    JSONObject jsonObject = new JSONObject(jsonData);
                    storeInfo.setName(jsonObject.getJSONArray("j").getJSONArray(8).getString(1));
                    storeInfo.setTel(jsonObject.getJSONArray("j").getJSONArray(8).getString(7));
                    try {
                        if (!storeInfo.getTel().equals("null")) {
                            String[] token = storeInfo.getTel().split(" ");
                            storeInfo.setTel("(0" + token[1] + ") " + token[2] + " " + token[3]);
                        }
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                    storeInfo.setRate(jsonObject.getJSONArray("j").getJSONArray(8).getString(3));
                    storeInfo.setStoreID(jsonObject.getJSONArray("j").getJSONArray(8).getJSONArray(0).getString(0));
                    String ReviewAmount = jsonObject.getJSONArray("j").getJSONArray(8).getString(4);
                    if(!ReviewAmount.equals("null"))
                        storeInfo.setReviewAmount(Integer.valueOf(ReviewAmount.split(" ")[0]));
                    else
                        storeInfo.setReviewAmount(0);
                    try {
                        onStoreListener.onInfoSuccess(storeInfo);
                        //getImageUrl(new URL("https://www.google.com/maps/uv?pb=!1s" + storeInfo.getStoreID() + "&hl=zh-Hant"));
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void getImageUrl(URL url) {
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .addHeader("User-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.4 (KHTML, like Gecko) Chrome/22.0.1229.94 Safari/537.4")
                .url(url)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonData = response.body().string();
                int index = jsonData.indexOf("window.APP_OPTIONS=") + 19;
                jsonData = "{\"j\":" +
                        jsonData.substring(index, jsonData.indexOf(";window.JS_VERSION=")) +
                        "}";
                try {
                    List<ImageAttr> Image = new ArrayList<>();
                    JSONObject jsonObject = new JSONObject(jsonData);
                    int len = jsonObject.getJSONArray("j").getJSONArray(7).getJSONArray(0).length();
                    for(int i = 0; i < len; i++) {
                        ImageAttr imageAttr = new ImageAttr();
                        if(!jsonObject.getJSONArray("j").getJSONArray(7).getJSONArray(0).getJSONArray(i).getJSONArray(6).getString(0).startsWith("//geo")) {
                            String imgURL = jsonObject.getJSONArray("j").getJSONArray(7).getJSONArray(0).getJSONArray(i).getJSONArray(6).getString(0);
                            imageAttr.setImageUrl(imgURL.substring(0, imgURL.indexOf("=")));
                            /* Google change it's json file, so I cannot get the provider and url. */
                            //try {
                            //    imageAttr.setProvider(jsonObject.getJSONArray("j").getJSONArray(7).getJSONArray(0)
                            //            .getJSONArray(i).getJSONArray(18)
                            //            .getJSONArray(0).getJSONArray(1)
                            //            .getString(1));
                            //    imageAttr.setOriginalSite(jsonObject.getJSONArray("j").getJSONArray(7)
                            //            .getJSONArray(0).getJSONArray(i).getJSONArray(18)
                            //            .getJSONArray(0).getJSONArray(1)
                            //            .getString(0));
                            //} catch (Exception e) {
                            imageAttr.setProvider("null");
                            imageAttr.setOriginalSite("null");
                            //}
                            Image.add(imageAttr);
                        }
                    }
                    storeInfo.setImage(Image);
                    onStoreListener.onInfoSuccess(storeInfo);
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}