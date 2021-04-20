package com.yuanxiang.gulimall.search.service;

import com.yuanxiang.gulimall.search.vo.SearchParam;
import com.yuanxiang.gulimall.search.vo.SearchResult;
import org.springframework.stereotype.Service;


public interface MallSearchService {
    SearchResult search(SearchParam searchParam);
}
