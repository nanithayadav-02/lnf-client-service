package com.technofacts.lnf.client.util;

import org.springframework.data.domain.Sort;

public final class RestUtil {

    private RestUtil() {
        throw new AssertionError();
    }

    public static Sort constructSort(final String sortBy, final String sortOrder) {
        Sort sortInfo = Sort.unsorted();
        if (sortBy != null) {
            sortInfo = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        }
        return sortInfo;
    }
}
