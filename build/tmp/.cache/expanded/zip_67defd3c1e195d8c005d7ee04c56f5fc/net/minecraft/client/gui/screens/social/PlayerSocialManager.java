package net.minecraft.client.gui.screens.social;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.UserApiService;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerSocialManager {
    private final Minecraft minecraft;
    private final Set<UUID> hiddenPlayers = Sets.newHashSet();
    private final UserApiService service;
    private final Map<String, UUID> discoveredNamesToUUID = Maps.newHashMap();
    private boolean onlineMode;
    private CompletableFuture<?> pendingBlockListRefresh = CompletableFuture.completedFuture(null);

    public PlayerSocialManager(Minecraft pMinecraft, UserApiService pService) {
        this.minecraft = pMinecraft;
        this.service = pService;
    }

    public void hidePlayer(UUID pId) {
        this.hiddenPlayers.add(pId);
    }

    public void showPlayer(UUID pId) {
        this.hiddenPlayers.remove(pId);
    }

    public boolean shouldHideMessageFrom(UUID pId) {
        return this.isHidden(pId) || this.isBlocked(pId);
    }

    public boolean isHidden(UUID pId) {
        return this.hiddenPlayers.contains(pId);
    }

    public void startOnlineMode() {
        this.onlineMode = true;
        this.pendingBlockListRefresh = this.pendingBlockListRefresh.thenRunAsync(this.service::refreshBlockList, Util.ioPool());
    }

    public void stopOnlineMode() {
        this.onlineMode = false;
    }

    public boolean isBlocked(UUID pId) {
        if (!this.onlineMode) {
            return false;
        } else {
            this.pendingBlockListRefresh.join();
            return this.service.isBlockedPlayer(pId);
        }
    }

    public Set<UUID> getHiddenPlayers() {
        return this.hiddenPlayers;
    }

    public UUID getDiscoveredUUID(String pUuid) {
        return this.discoveredNamesToUUID.getOrDefault(pUuid, Util.NIL_UUID);
    }

    public void addPlayer(PlayerInfo pPlayerInfo) {
        GameProfile gameprofile = pPlayerInfo.getProfile();
        this.discoveredNamesToUUID.put(gameprofile.getName(), gameprofile.getId());
        if (this.minecraft.screen instanceof SocialInteractionsScreen socialinteractionsscreen) {
            socialinteractionsscreen.onAddPlayer(pPlayerInfo);
        }
    }

    public void removePlayer(UUID pId) {
        if (this.minecraft.screen instanceof SocialInteractionsScreen socialinteractionsscreen) {
            socialinteractionsscreen.onRemovePlayer(pId);
        }
    }
}