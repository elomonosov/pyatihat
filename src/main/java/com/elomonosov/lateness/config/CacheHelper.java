package com.elomonosov.lateness.config;

import org.springframework.stereotype.Component;

@Component
public class CacheHelper {

    /*private PersistentCacheManager persistentCacheManager;
    private String cacheName = "debtorSumCache";

    public CacheHelper() {

        persistentCacheManager =
                CacheManagerBuilder.newCacheManagerBuilder()
                        .with(CacheManagerBuilder.persistence("lateness-storage/"
                                + File.separator
                                + "debtorName"))
                        .withCache(cacheName, CacheConfigurationBuilder
                                .newCacheConfigurationBuilder(String.class, Integer.class,
                                        ResourcePoolsBuilder.newResourcePoolsBuilder()
                                                .heap(0, EntryUnit.ENTRIES)
                                                .disk(10, MemoryUnit.MB, true))
                        )
                        .build(true);

    }

    public Cache<String, Integer> getDebtorSum() {
        return persistentCacheManager.getCache(cacheName, String.class, Integer.class);
    }*/

}
