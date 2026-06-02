package com.auction.server.service;

import com.auction.server.domain.model.Auction;
import com.auction.server.domain.model.GenericItem;
import com.auction.server.domain.model.Item;
import com.auction.server.domain.model.Seller;
import com.auction.common.AuctionTime;
import com.auction.common.ProductStatus;

final class DomainAuctionMapper {

    Auction toDomainAuction(BidService.ProductSnapshot product) {
        Item item = new GenericItem(product.name, product.startPrice);
        Seller seller = new Seller(product.sellerUsername);

        Auction auction = new Auction(
                item,
                seller,
                product.startTime,
                product.endTime
        );

        auction.restoreCurrentPrice(product.currentPrice);

        if (ProductStatus.isFinished(product.status, product.endTime, AuctionTime.now())) {
            auction.finish();
        }

        return auction;
    }
}
