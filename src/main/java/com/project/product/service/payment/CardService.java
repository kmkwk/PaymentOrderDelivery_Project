package com.project.product.service.payment;

import com.project.product.domain.payment.Card;
import com.project.product.dto.order.OrderCreateRequest;
import com.project.product.dto.payment.CardRegisterRequest;
import com.project.product.dto.payment.MemberCardListResponse;
import com.project.product.exception.ClientException;
import com.project.product.exception.ErrorCode;
import com.project.product.repository.payment.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardService implements PaymentService<Object>{

    private final CardRepository cardRepository;

    @Transactional
    public Card registerCard(CardRegisterRequest cardRegisterRequest) {
        Card card = Card.cardBuilder(cardRegisterRequest);
        return cardRepository.save(card);
    }

    public List<MemberCardListResponse> findMemberCardList(Long memberId){
        List<Card> cards = cardRepository.findAllByMemberId(memberId);

        return MemberCardListResponse.memberCardListDtoBuilder(cards);
    }

    @Transactional
    @Override
    public LocalDateTime payment(OrderCreateRequest orderCreateRequest, int restPrice) {
        Card card = cardRepository.findById(orderCreateRequest.getCardId())
                .orElseThrow(() -> new ClientException(ErrorCode.NOT_FOUND_CARD));

        if(card.cardStatusCheck(card.getCardStatus())
                && card.cardPaymentCheck(card.getMoney(),orderCreateRequest.getTotalPrice(),restPrice)){

            card.cardPayment(restPrice);

            return LocalDateTime.now();
        }

        throw new ClientException(ErrorCode.REJECT_ACCOUNT_PAYMENT);
    }

}
