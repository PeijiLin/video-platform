package com.lpjpro.pojo.model;

import java.util.Map;

public class UserSimilarity {
        private final long userId;
        private final double similarity;
        private final Map<String, Double> features;

        public UserSimilarity(long userId, double similarity, Map<String, Double> features) {
            this.userId = userId;
            this.similarity = similarity;
            this.features = features;
        }

        public long getUserId() { return userId; }
        public double getSimilarity() { return similarity; }
        public Map<String, Double> getFeatures() { return features; }
    }