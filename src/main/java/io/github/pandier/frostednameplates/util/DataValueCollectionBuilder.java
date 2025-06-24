package io.github.pandier.frostednameplates.util;

import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Internal
public class DataValueCollectionBuilder {
    private final List<WrappedDataValue> list = new ArrayList<>();

    public <T> DataValueCollectionBuilder add(int index, Class<T> type, T value) {
        return add(index, WrappedDataWatcher.Registry.get(type), value);
    }

    public DataValueCollectionBuilder add(int index, WrappedDataWatcher.Serializer serializer, Object value) {
        list.add(new WrappedDataValue(index, serializer, value));
        return this;
    }

    public List<WrappedDataValue> build() {
        return list;
    }
}
