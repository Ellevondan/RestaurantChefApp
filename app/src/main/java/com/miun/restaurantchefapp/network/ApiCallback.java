package com.miun.restaurantchefapp.network;

/**
 * Callback-interface för att hantera API-svar och fel
 */
public interface ApiCallback<T> {

    /**
     * Anropas när API-anropet lyckas
     * @param result Resultatet från API:et
     */
    void onSuccess(T result);

    /**
     * Anropas när API-anropet misslyckas
     * @param errorMessage Felmeddelande
     */
    void onError(String errorMessage);
}