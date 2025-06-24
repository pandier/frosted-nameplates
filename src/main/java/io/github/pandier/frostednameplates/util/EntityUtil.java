package io.github.pandier.frostednameplates.util;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.atomic.AtomicInteger;

@ApiStatus.Internal
public final class EntityUtil {

    public static int generateEntityId() {
        Class<?> entityClass = MinecraftReflection.getEntityClass();
        FieldAccessor fieldAccessor = Accessors.getFieldAccessor(FuzzyReflection.fromClass(entityClass, true)
                .getField(FuzzyFieldContract.newBuilder().typeExact(AtomicInteger.class).build()));
        return ((AtomicInteger) fieldAccessor.get(null)).incrementAndGet();
    }
}
