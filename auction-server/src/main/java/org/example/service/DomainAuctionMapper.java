package org.example.service;

import com.auction.logic.model.Auction;
import com.auction.logic.model.GenericItem;
import com.auction.logic.model.Item;
import com.auction.logic.model.Seller;
import org.example.common.AuctionTime;
import org.example.common.ProductStatus;

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
