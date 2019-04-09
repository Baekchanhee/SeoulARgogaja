package cau.seoulargogaja;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cau.seoulargogaja.adapter.SearchListAdapter;
import cau.seoulargogaja.data.SpotDAO;
import cau.seoulargogaja.data.SpotDTO;
import cau.seoulargogaja.data.SpotParser;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener{

    public static ArrayList<SpotDTO> list = null;

    Button keywordSearchBtn;
    SearchView searchView;

    SearchListAdapter searchListAdapter;
    ListView searchListView;
    List<SpotDTO> searchedList = new ArrayList<>();

    InputMethodManager imm;



    public void onClick(View v) {

        if(v == keywordSearchBtn) {
            String query =  searchView.getQuery().toString();
            submitQuery(query);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);


        //앱 최초 실행 시 db 생성
        SharedPreferences pref = getSharedPreferences("isFirst", Activity.MODE_PRIVATE);
        boolean first = pref.getBoolean("isFirst", false);
        if(first==false) {
            Log.d("tour", "[first] 첫 실행...");
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("isFirst", true);
            editor.commit();

            SpotParser parser = new SpotParser();
            try {
                parser.start();
                parser.join(); // 서버 xml파일 파서

                SpotDAO dao = new SpotDAO(this); // db 생성
                dao.createTable();

                list = parser.getList();
                dao.setData(list);
            } catch (InterruptedException e){
                e.printStackTrace();
            }




        }
        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        keywordSearchBtn = (Button) findViewById(R.id.keyword_search_btn);
        keywordSearchBtn.setOnClickListener(this);

        searchListView = (ListView) findViewById(R.id.search_list);


        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                submitQuery(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void submitQuery(String query) {
        hideKeyboard();
        searchedList = searchByKeyword(query);
        if (searchedList.size() >= 1) {
            searchListAdapter = new SearchListAdapter(getApplicationContext(), searchedList);
            searchListView.setAdapter(searchListAdapter);
            searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                    SpotDTO tour = searchedList.get(position);
                    /*Utils.toast(getApplication(), "Show Detail : " + tour.getName());*/
                    Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                    intent.putExtra("name", tour.getName());
                    startActivity(intent);
                }
            });
        } else if (searchedList.size() == 0) {
           Toast.makeText(getApplicationContext(), "검색 결과가 없습니다", Toast.LENGTH_LONG).show();
        }
    }

    private void hideKeyboard() {
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
    }





    public List<SpotDTO> getTourList() {
        SpotDAO dao = new SpotDAO(this);
        return dao.selectAll();
    }


    public List<SpotDTO> searchByKeyword(String query) {
        List<SpotDTO> result = new ArrayList<>();
        List<SpotDTO> tourList = getTourList();

//        Utils.log("# SEARCH BY KEYWORD: " + query + " in " + tourList.size());
        for (SpotDTO tour : tourList) {

            String target = tour.getName().replaceAll("\\s", "");
            query = query.replaceAll("\\s", "");
            if (target.equalsIgnoreCase(query) || target.toLowerCase().contains(query.toLowerCase())) {
                result.add(tour);
            }
        }
//        Utils.log("# SEARCH RESULT " + result.size() + "개");
        return result;
    }

}