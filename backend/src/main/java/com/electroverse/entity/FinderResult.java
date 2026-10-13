package com.electroverse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "finder_results", indexes = {
        @Index(name = "idx_finder_results_session", columnList = "session_id")
})
public class FinderResult extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private FinderSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private double score;

    @Column(name = "rank_position", nullable = false)
    private int rankPosition;

    @Column(length = 2000)
    private String explanation;

    @Column(name = "spec_matches", length = 2000)
    private String specMatches;

    @Column(name = "within_budget", nullable = false)
    private boolean withinBudget;

    public FinderSession getSession() {
        return session;
    }

    public void setSession(FinderSession session) {
        this.session = session;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getRankPosition() {
        return rankPosition;
    }

    public void setRankPosition(int rankPosition) {
        this.rankPosition = rankPosition;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getSpecMatches() {
        return specMatches;
    }

    public void setSpecMatches(String specMatches) {
        this.specMatches = specMatches;
    }

    public boolean isWithinBudget() {
        return withinBudget;
    }

    public void setWithinBudget(boolean withinBudget) {
        this.withinBudget = withinBudget;
    }
}
