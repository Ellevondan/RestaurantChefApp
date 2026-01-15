package com.miun.restaurantchefapp.network;

import com.miun.restaurantchefapp.models.CarteMenuItem;
import com.miun.restaurantchefapp.models.OrderBundle;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface KitchenApiService {

    /**
     * Hämtar alla aktiva ordrar från köket
     */
    @GET("forwardsTo")
    Call<List<OrderBundle>> getActiveOrders();

    /**
     * Kontrollerar om en specifik order är klar
     */
    @GET("forwardsTo/{id}/isDone")
    Call<IsDoneResponse> checkIfOrderDone(@Path("id") int id);

    /**
     * Markerar en order som klar
     */
    @PUT("orders/{id}/reset")
    Call<Void> markOrderComplete(@Path("id") int id);

    /**
     * Hämtar hela carte-menyn med alla tillgängliga rätter och deras tidsinformation
     * Detta används för att få activeTime och waitingTime för varje rätt
     */
    @GET("carte-menu/active")
    Call<List<CarteMenuItem>> getActiveCarteMenu();
}