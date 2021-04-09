package com.dd.dda.thread;

import com.dd.dda.model.sqldata.Bill;
import com.dd.dda.repository.BillRepository;
import com.dd.dda.repository.UserBillVoteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Scope("prototype")
public class BillRankingThread extends Thread{

    private List<Long> ids;
    private double avgLikeRatio;
    private BillRepository billRepository;
    private UserBillVoteRepository userBillVoteRepository;

    public void init(List<Long> ids, double avgLikeRatio, BillRepository billRepository, UserBillVoteRepository userBillVoteRepository){
        this.ids = ids;
        this.avgLikeRatio = avgLikeRatio;
        this.billRepository = billRepository;
        this.userBillVoteRepository = userBillVoteRepository;

    }

    @Override
    public void run() {
        //TODO nochmal mit negativ gerankter Diskussion testen
        log.info("start reranking thread");
        List<Bill> bills = billRepository.findAllById(ids);
        //EntityManager ?
        List<Object> upvotes = userBillVoteRepository.getNrOfUpvotesFor(ids);
        List<Object> downvotes = userBillVoteRepository.getNrOfDownvotesFor(ids);
        Map<Long, Long> upvotesMap = new HashMap<>();
        Map<Long, Long> downvotesMap = new HashMap<>();
        for(Object o : upvotes){
            Object[] u =  (Object[]) o;
            Long a = ((java.math.BigInteger) u[0]).longValue();
            Long b = ((java.math.BigInteger) u[1]).longValue();
            upvotesMap.put( a,b);
        }
        for(Object o : downvotes){
            Object[] u =  (Object[]) o;
            Long a = ((java.math.BigInteger) u[0]).longValue();
            Long b = ((java.math.BigInteger) u[1]).longValue();
            downvotesMap.put( a,b);
        }
        bills.stream().parallel().forEach(b -> {
            Long upv = upvotesMap.get(b.getId());
            if(upv == null){
                upv = 0L;
            }
            if(b.getParliament_role() == 2){
                Long dv = downvotesMap.get(b.getId());
                if(dv != null){
                    upv = upv - dv;
                }
            }
            b.rank(upv, avgLikeRatio);
        });
        billRepository.saveAll(bills);
        log.info("finish reranking thread");
    }
}
