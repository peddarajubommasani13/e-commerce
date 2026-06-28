package com.fashion.store.dto;

import lombok.*;

public class CategoryDTO {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CategoryResponse {
        private Long id;
        private String name;
        private String slug;
    }
}
