package io.github.pandier.frostednameplates.internal.packet.render;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.pandier.frostednameplates.internal.NameplateState;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Optional;

@ApiStatus.Internal
public class NameplateRenderer {
    private static final int FLAGS_DATA_INDEX = 0;
    private static final int CUSTOM_NAME_DATA_INDEX = 2;
    private static final int CUSTOM_NAME_VISIBLE_DATA_INDEX = 3;
    private static final int TEXT_DISPLAY_BACKGROUND_DATA_INDEX = 25;

    public RenderedNameplateState create(User user, int targetId, Vector3d position, NameplateState state) {
        RenderedNameplateState renderState = new RenderedNameplateState(targetId);
        renderState.visible = state.visible();

        user.sendPacket(new WrapperPlayServerSpawnEntity(renderState.entityId, Optional.of(renderState.entityUuid), EntityTypes.TEXT_DISPLAY,
                position.add(0.0, 1.8, 0.0), 0f, 0f, 0f, 0, Optional.of(new Vector3d())));
        user.sendPacket(new WrapperPlayServerEntityMetadata(renderState.entityId, List.of(
                new EntityData<>(FLAGS_DATA_INDEX, EntityDataTypes.BYTE, getFlags(false)),
                new EntityData<>(CUSTOM_NAME_DATA_INDEX, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(state.text())),
                new EntityData<>(CUSTOM_NAME_VISIBLE_DATA_INDEX, EntityDataTypes.BOOLEAN, renderState.isVisible()),
                // Setting the background color to 0 will make the text display not display anything, while still showing the custom name
                new EntityData<>(TEXT_DISPLAY_BACKGROUND_DATA_INDEX, EntityDataTypes.INT, 0)
        )));

        return renderState;
    }

    public void updateStatus(User user, RenderedNameplateState renderState, boolean sneaking, boolean invisible) {
        renderState.invisibleStatus = invisible;
        user.sendPacket(new WrapperPlayServerEntityMetadata(renderState.entityId, List.of(
                new EntityData<>(FLAGS_DATA_INDEX, EntityDataTypes.BYTE, getFlags(sneaking)),
                new EntityData<>(CUSTOM_NAME_VISIBLE_DATA_INDEX, EntityDataTypes.BOOLEAN, renderState.isVisible())
        )));
    }

    public void update(User user, RenderedNameplateState renderState, NameplateState state) {
        renderState.visible = state.visible();
        user.sendPacket(new WrapperPlayServerEntityMetadata(renderState.entityId, List.of(
                new EntityData<>(CUSTOM_NAME_DATA_INDEX, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(state.text())),
                new EntityData<>(CUSTOM_NAME_VISIBLE_DATA_INDEX, EntityDataTypes.BOOLEAN, renderState.isVisible())
        )));
    }

    public void remove(User user, RenderedNameplateState renderState) {
        user.sendPacket(new WrapperPlayServerDestroyEntities(renderState.entityId));
    }

    private static byte getFlags(boolean sneaking) {
        // 32 = invisible, 2 = sneaking
        return (byte) (32 | (sneaking ? 2 : 0));
    }
}
