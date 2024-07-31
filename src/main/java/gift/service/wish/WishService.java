package gift.service.wish;

import gift.dto.paging.PagingResponse;
import gift.dto.wish.WishResponse;
import gift.exception.ProductNotFoundException;
import gift.exception.WishNotFoundException;
import gift.model.product.Product;
import gift.model.user.User;
import gift.model.wish.Wish;
import gift.repository.product.ProductRepository;
import gift.repository.user.UserRepository;
import gift.repository.wish.WishRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishService {

    private final WishRepository wishRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Autowired
    public WishService(WishRepository wishRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.wishRepository = wishRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public void addGiftToUser(Long userId, Long giftId, int quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Product product = productRepository.findById(giftId)
                .orElseThrow(() -> new ProductNotFoundException("존재하지 않는 상품입니다."));

        wishRepository.findByUserAndProduct(user, product)
                .ifPresentOrElse(
                        existingWish -> {
                            existingWish.increaseQuantity();
                            wishRepository.save(existingWish);
                        },
                        () -> {
                            Wish userGift = new Wish(user, product, quantity);
                            wishRepository.save(userGift);
                        }
                );
    }

    @Transactional
    public void removeGiftFromUser(Long userId, Long wishId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Wish wish =  wishRepository.findById(wishId)
                .orElseThrow(() -> new WishNotFoundException("존재하지 않는 위시리스트입니다."));
        wishRepository.deleteByUserAndId(user, wishId);
    }

    public PagingResponse<WishResponse.Info> getGiftsFromUser(Long userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").ascending());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Page<Wish> wishes = wishRepository.findByUser(user, pageRequest);
        List<WishResponse.Info> wishResponses = wishes.getContent()
                .stream()
                .map(wish -> new WishResponse.Info(wish.getId(), wish.getProduct().getId(), wish.getProduct().getName(), wish.getProduct().getPrice(), wish.getProduct().getImageUrl()))
                .collect(Collectors.toList());
        return new PagingResponse<>(page, wishResponses, size, wishes.getTotalElements(), wishes.getTotalPages());
    }

    @Transactional
    public void updateWishQuantity(Long userId, Long giftId, int quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Product product = productRepository.findById(giftId)
                .orElseThrow(() -> new ProductNotFoundException("존재하지 않는 상품입니다."));
        wishRepository.findByUserAndProduct(user, product)
                .ifPresentOrElse(
                        existingWish -> {
                            existingWish.modifyQuantity(quantity);
                            wishRepository.save(existingWish);
                        },
                        () -> {
                            throw new WishNotFoundException("존재하지 않는 위시리스트입니다.");
                        }
                );
    }
}