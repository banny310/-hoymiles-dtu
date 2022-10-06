package com.hoymiles.infrastructure.dtu;

import com.hoymiles.infrastructure.repository.SpreadsheetWriter;
import com.typesafe.config.Config;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.BeanManager;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Dependent
@RequiredArgsConstructor
public class DtuMessageRouter {
    @FunctionalInterface
    public interface Mapper {
        Object mapToEvent(DtuMessage event);
    }

    private final BeanManager beanManager;
    private final Config config;
    private final SpreadsheetWriter spreadsheetWriter;
    private final Map<Integer, Mapper> mapperMap = new HashMap<>();

    public void handle(@NotNull DtuMessage event) {
        if (config.getBoolean("app.store_messages_in_excel")) {
            spreadsheetWriter.write(event.getCode(), event.getMessage());
        }

        if (mapperMap.containsKey(event.getCode())) {
            Object domainEvent = mapperMap.get(event.getCode()).mapToEvent(event);
            beanManager.fireEvent(domainEvent);
        }
    }

    public void register(int code, Mapper mapper) {
        mapperMap.put(code, mapper);
    }
}
