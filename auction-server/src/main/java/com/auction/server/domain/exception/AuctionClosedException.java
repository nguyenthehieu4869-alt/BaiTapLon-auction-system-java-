package com.auction.server.domain.exception;

public class AuctionClosedException extends Exception {
  public AuctionClosedException(String msg) { super(msg); }
}