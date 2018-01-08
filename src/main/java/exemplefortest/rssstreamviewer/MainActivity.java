package exemplefortest.rssstreamviewer;

import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    String UrlFlux="flux.20minutes.fr/feeds/rss-monde.xml";
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeLayout;
    BottomNavigationView bottomNavigationView;
    boolean clicknews=true;
    String ActualUrl;





    private List<RssFeedModel> mFeedModelList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);


        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));



        bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {

                    final SwipeRefreshLayout swipenews=(SwipeRefreshLayout)findViewById(R.id.swipeRefreshLayout);
                    LinearLayout webArticle=(LinearLayout)findViewById(R.id.webv);
                    LinearLayout prev=(LinearLayout)findViewById(R.id.prev);

                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_back:


                                swipenews.setVisibility(View.VISIBLE);
                                prev.setVisibility(View.GONE);
                                webArticle.setVisibility(View.GONE);
                                bottomNavigationView.setVisibility(View.GONE);
                                clicknews=true;
                                ActualUrl=null;
                                Log.i("menu bottom", "back");
                                break;


                            case R.id.action_read:


                                webArticle.setVisibility(View.VISIBLE);
                                swipenews.setVisibility(View.GONE);
                                prev.setVisibility(View.GONE);
                                WebView webView = (WebView) findViewById(R.id.Webv);
                                webView.getSettings().setJavaScriptEnabled(true);
                                webView.getSettings().setPluginState(WebSettings.PluginState.ON);
                                webView.getSettings().setUseWideViewPort(true);
                                webView.loadUrl(ActualUrl);
                                Log.i("menu bottom", "Read : "+ActualUrl);
                                break;
                        }
                        return true;
                    }
                });



        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new FetchFeedTask().execute((Void) null);
            }

        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    Log.i("TAG","SCroll up");
                } else {
                    Log.i("TAG","SCroll down");
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    Log.i("TAG","Do 1");
                } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    Log.i("TAG","Do 2");
                } else {
                    Log.i("TAG","Do 3");
                }
            }
        });

        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        if(clicknews) {
                            RssFeedModel mflux = mFeedModelList.get(position);
                            LaunchPrev(mflux.getTitle(), mflux.getImg(), mflux.getdesc());

                            int linkPos=position-1;
                            RssFeedModel linflux = mFeedModelList.get(linkPos);
                            ActualUrl=linflux.getlink();

                        }

                    }

                })
        );


    }






    @Override
    protected void onStart() {
        super.onStart();

        new FetchFeedTask().execute((Void) null);
    }


    private void LaunchPrev(String Title, String img, String prevArt){
        bottomNavigationView.setVisibility(View.VISIBLE);
        LinearLayout prev=(LinearLayout)findViewById(R.id.prev);
        TextView title=(TextView)findViewById(R.id.Title);
        TextView prevArticle=(TextView)findViewById(R.id.prevart);
        ImageView imgprev=(ImageView)findViewById(R.id.imgprev);

        clicknews=false;
        title.setText(Title);
        Picasso.with(getApplicationContext()).load(img).into(imgprev);
        prevArticle.setText(prevArt);

        prev.setVisibility(View.VISIBLE);


    }







    private class FetchFeedTask extends AsyncTask<Void, Void, Boolean> {

        private String urlLink;

        @Override
        protected void onPreExecute() {
            mSwipeLayout.setRefreshing(true);
            urlLink =UrlFlux;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (TextUtils.isEmpty(urlLink))
                return false;

            try {
                if(!urlLink.startsWith("http://") && !urlLink.startsWith("https://"))
                    urlLink = "http://" + urlLink;

                URL url = new URL(urlLink);
                InputStream inputStream = url.openConnection().getInputStream();
                mFeedModelList = parseFeed(inputStream);
                return true;
            } catch (IOException e) {
                Log.e("", "Error", e);
            } catch (XmlPullParserException e) {
                Log.e("", "Error", e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mSwipeLayout.setRefreshing(false);

            if (success) {
                // Fill RecyclerView
                mRecyclerView.setAdapter(new RssFeedListAdapter(mFeedModelList));
            } else {
                Toast.makeText(MainActivity.this,
                        "Enter a valid Rss feed url",
                        Toast.LENGTH_LONG).show();
            }
        }
    }



    public List<RssFeedModel> parseFeed(InputStream inputStream) throws XmlPullParserException,
            IOException {
        String title = null;
        String link = null;
        String date = null;
        String desc=null;
        String Img=null;
        boolean isItem = false;
        List<RssFeedModel> items = new ArrayList<>();


        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlPullParser.setInput(inputStream, null);

            xmlPullParser.nextTag();
            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {

                int eventType = xmlPullParser.getEventType();

                String name = xmlPullParser.getName();
                if (name == null)
                    continue;

                if (eventType == XmlPullParser.END_TAG) {
                    if (name.equalsIgnoreCase("item")) {
                        isItem = false;
                    }
                    continue;
                }

                if (eventType == XmlPullParser.START_TAG) {
                    if (name.equalsIgnoreCase("item")) {
                        isItem = true;
                        continue;
                    }
                }


                Log.i("MyXmlParser", "Parsing name ==> " + name);
                String result = "";
                if (xmlPullParser.next() == XmlPullParser.TEXT) {
                    result = xmlPullParser.getText();
                    xmlPullParser.nextTag();
                }

                if (name.equalsIgnoreCase("link")) {
                    link = result;
                    Log.i("URL","Result : "+result);
                } else if (name.equalsIgnoreCase("pubDate")) {
                    date = result;
                } else if (name.equalsIgnoreCase("title")) {
                    title = result;
                } else if (name.equalsIgnoreCase("description")) {
                    desc = result;
                }else if (name.equalsIgnoreCase("enclosure")) {
                    Img = xmlPullParser.getAttributeValue(null, "url");
                }


                if (title != null && link != null && date != null&&desc!=null) {
                    if(isItem) {
                        RssFeedModel item = new RssFeedModel(title, date, desc, link, Img);
                        items.add(item);
                         }


                    title = null;
                    link = null;
                    date = null;
                    isItem = false;
                }
            }


            return items;
        } finally {
            inputStream.close();
        }
    }



}

