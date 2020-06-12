package com.cjnet.payme.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.cjnet.payme.R;
import com.cjnet.payme.ui.adapter.ProdcutsAdapter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HomeFragment extends Fragment implements
        ProdcutsAdapter.doBilling, PurchasesUpdatedListener {


    private HomeViewModel homeViewModel;
    private BillingClient billingClient;
    private List<String> skuIds = new ArrayList<>();
    private RecyclerView recyclerView;
    private ProdcutsAdapter prodcutsAdapter;
    private List<String> purchasedSKUs = new ArrayList<>();

    @Override
    public void startBilling(SkuDetails skuID) {

        billingClient.launchBillingFlow(getActivity(), BillingFlowParams
                .newBuilder()
                .setSkuDetails(skuID).build());

        Log.d(HomeFragment.class.getSimpleName(), "Billing started");

    }

    @Override
    public void onResume() {
        super.onResume();
        setupBillingClient();
        Log.d(HomeFragment.class.getSimpleName(), "Screen Resumed");
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {

        Log.d(HomeFragment.class.getSimpleName(), "Purchase Updated");
        //allowMultiplePurchase(purchases);
    }

    private void allowMultiplePurchase(List<Purchase> purchases) {
        if (purchases.get(purchases.size() - 1) != null) {
            billingClient.consumeAsync(purchases.get(purchases.size() - 1).getPurchaseToken(),
                    new ConsumeResponseListener() {
                        @Override
                        public void onConsumeResponse(int responseCode, String purchaseToken) {
                            Log.d(HomeFragment.class.getSimpleName(), "Purchase Consumed");
                        }
                    });
        }
        Log.d(HomeFragment.class.getSimpleName(), purchases.toString());
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //setupBillingClient();
        setupSKUs();
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        root.findViewById(R.id.loadProducts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoadProductsClicked();
            }
        });

        root.findViewById(R.id.clearPurchase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearHistory();
            }
        });

        recyclerView = root.findViewById(R.id.products);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        return root;

    }

    private void setupSKUs() {
        skuIds.add("cjnet_gen_1");
        skuIds.add("cjnet_ad_1");
    }

    public void onLoadProductsClicked() {

        if (billingClient.isReady()) {
            Log.d(HomeFragment.class.getSimpleName(), "Billing is Ready");
            SkuDetailsParams skuDetailsParams = SkuDetailsParams
                    .newBuilder()
                    .setSkusList(skuIds)
                    .setType(BillingClient.SkuType.INAPP)
                    .build();

            billingClient.querySkuDetailsAsync(skuDetailsParams, new SkuDetailsResponseListener() {
                @Override
                public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                    initProductAdapter(skuDetailsList);
                }
            });
        } else {
            Log.d(HomeFragment.class.getSimpleName(), "Billing is not Ready");
        }
    }

    public void initProductAdapter(List<SkuDetails> skuIds) {

        prodcutsAdapter = new ProdcutsAdapter(skuIds, this, getPurchases());
        recyclerView.setAdapter(prodcutsAdapter);
    }

    public void setupBillingClient() {

        billingClient = BillingClient
                .newBuilder(getContext())
                .setListener(new PurchasesUpdatedListener() {
                    @Override
                    public void onPurchasesUpdated(int responseCode,
                                                   @Nullable List<Purchase> purchases) {
                        if (purchases != null) {
                            if (purchases.size() > 0) {
                                for (int i = 0; i < purchases.size(); i++) {
                                    Log.d(HomeFragment.class.getSimpleName(), "Purchased SKUs-> "
                                            + purchases.get(i).getSku());

                                }
                            } else {

                                Log.d(HomeFragment.class.getSimpleName(), "No SKUs Purchased");
                            }
                        }
                    }
                }).build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(int responseCode) {
                if (responseCode == BillingClient.BillingResponse.OK) {
                    Log.d(HomeFragment.class.getSimpleName(), "Billing Setup Success");
                    getPurchases();
                    //refresh screen
                    onLoadProductsClicked();
                } else {
                    Log.d(HomeFragment.class.getSimpleName(), "Billing Setup FAILURE with ->" + responseCode);
                }
                //clearHistory();
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.d(HomeFragment.class.getSimpleName(), "BILLING DISCONNECTED");
            }
        });
    }

    public void clearHistory() {
        if (billingClient != null) {
            List<Purchase> purchases =
                    billingClient.queryPurchases(BillingClient.SkuType.INAPP).getPurchasesList();
            if (purchases != null) {
                for (int i = 0; i < purchases.size(); i++) {
                    billingClient.consumeAsync(purchases.get(i).getPurchaseToken(), new ConsumeResponseListener() {
                        @Override
                        public void onConsumeResponse(int responseCode, String purchaseToken) {
                            Log.d(HomeFragment.class.getSimpleName(),
                                    "Purchase Token -> " +
                                            purchaseToken +
                                            " -> Consumed in Clear History");
                        }
                    });
                }
            }
        } else {
            Log.d(HomeFragment.class.getSimpleName(), "Billing Client empty in Clear History");
        }
    }

    public List<String> getPurchases() {

        if (billingClient != null) {
            List<Purchase> purchases =
                    billingClient.queryPurchases(BillingClient.SkuType.INAPP).getPurchasesList();
            if (purchases != null) {
                if (purchases.size() > 0) {
                    purchasedSKUs = new ArrayList<>();
                    for (int i = 0; i < purchases.size(); i++) {
                        Log.d(HomeFragment.class.getSimpleName(),
                                "Purchase Token from History -> " +
                                        purchases.get(i).getPurchaseToken());
                        purchasedSKUs.add(purchases.get(i).getSku());
                    }
                } else {
                    Log.d(HomeFragment.class.getSimpleName(), "No Purchases History till now");
                }
            }
        } else {
            Log.d(HomeFragment.class.getSimpleName(), "Billing Client empty in Purchase History");
        }
        return purchasedSKUs;
    }

}