package org.example.network.protocol;
public enum MessageType {

    // Auth
    LOGIN,
    REGISTER,
    LOGIN_SUCCESS,
    REGISTER_SUCCESS,
    LOGIN_FAIL,
    REGISTER_FAIL,

    // Product
    GET_PRODUCTS,
    PRODUCT_LIST,

    // Bid
    PLACE_BID,
    BID_SUCCESS,
    BID_FAIL,
    BID_UPDATE
}

