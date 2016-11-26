package me.zsj.interessant;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.List;

import me.drakeet.multitype.Items;
import me.drakeet.multitype.MultiTypeAdapter;
import me.zsj.interessant.api.RelatedApi;
import me.zsj.interessant.provider.daily.ItemList;
import me.zsj.interessant.provider.related.CardItem;
import me.zsj.interessant.provider.related.HeaderItem;
import me.zsj.interessant.provider.related.RelatedHeaderItem;
import me.zsj.interessant.rx.ErrorAction;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author zsj
 */

public class RelatedActivity extends RxAppCompatActivity {

    public static final String ID = "id";
    public static final String RELATED_VIDEO = "related";

    private MultiTypeAdapter adapter;
    private RecyclerView list;
    private Items items = new Items();
    private RelatedApi relatedApi;

    private int id;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.related_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> finish());

        list = (RecyclerView) findViewById(R.id.related_list);

        adapter = new MultiTypeAdapter(items);
        list.setAdapter(adapter);

        Register.registerRelatedItem(adapter, this);

        id = getIntent().getIntExtra(ID, id);
        relatedApi = InteressantFactory.getRetrofit().createApi(RelatedApi.class);

        loadRelated();
    }

    private void loadRelated() {
        relatedApi.related(id)
                .compose(bindToLifecycle())
                .filter(related -> related != null)
                .filter(related -> related.itemList != null)
                .map(related -> related.itemList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::addData, throwable -> ErrorAction.errorAction(this));
    }

    private void addData(List<ItemList> itemLists) {
        for (ItemList item : itemLists) {
            if (item.data.header.description != null) {
                items.add(new HeaderItem(item.data.header));
            } else {
                items.add(new RelatedHeaderItem(item.data.header));
            }
            items.add(new CardItem(item));
        }
        adapter.notifyDataSetChanged();
    }

}