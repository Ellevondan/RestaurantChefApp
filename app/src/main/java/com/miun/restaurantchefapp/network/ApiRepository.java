package com.miun.restaurantchefapp.network;

import android.util.Log;
import com.miun.restaurantchefapp.models.OrderBundle;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository-klass som hanterar all kommunikation med API:et
 */
public class ApiRepository {

    private static final String TAG = "ApiRepository";
    private final KitchenApiService apiService;

    public ApiRepository() {
        this.apiService = ApiClient.getKitchenApiService();
    }

    /**
     * Hämtar alla aktiva ordrar från API:et
     */
    public void getActiveOrders(ApiCallback<List<OrderBundle>> callback) {
        Call<List<OrderBundle>> call = apiService.getActiveOrders();

        call.enqueue(new Callback<List<OrderBundle>>() {
            @Override
            public void onResponse(Call<List<OrderBundle>> call, Response<List<OrderBundle>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Hämtade " + response.body().size() + " aktiva ordrar");
                    callback.onSuccess(response.body());
                } else {
                    Log.e(TAG, "API-fel: " + response.code());
                    callback.onError("Kunde inte hämta ordrar: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<OrderBundle>> call, Throwable t) {
                Log.e(TAG, "Nätverksfel: " + t.getMessage());
                callback.onError("Nätverksfel: " + t.getMessage());
            }
        });
    }

    /**
     * Kontrollerar om en order är klar
     */
    public void checkIfOrderDone(int orderId, ApiCallback<Boolean> callback) {
        Call<IsDoneResponse> call = apiService.checkIfOrderDone(orderId);

        call.enqueue(new Callback<IsDoneResponse>() {
            @Override
            public void onResponse(Call<IsDoneResponse> call, Response<IsDoneResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().isDone());
                } else {
                    callback.onError("Kunde inte kontrollera orderstatus: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<IsDoneResponse> call, Throwable t) {
                callback.onError("Nätverksfel: " + t.getMessage());
            }
        });
    }

    /**
     * Markerar en order som klar
     * OBS: Kräver att du lägger till en PUT-endpoint i ditt backend API
     */
    public void markOrderComplete(int orderId, ApiCallback<Void> callback) {
        Call<Void> call = apiService.markOrderComplete(orderId);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Order " + orderId + " markerad som klar");
                    callback.onSuccess(null);
                } else {
                    callback.onError("Kunde inte markera order som klar: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Nätverksfel: " + t.getMessage());
            }
        });
    }
}