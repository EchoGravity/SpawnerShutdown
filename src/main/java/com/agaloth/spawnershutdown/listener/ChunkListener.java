package com.agaloth.spawnershutdown.listener;

import com.agaloth.spawnershutdown.SpawnerShutdown;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;

public class ChunkListener implements Listener {

    private SpawnerShutdown plugin;

    public ChunkListener(SpawnerShutdown plugin) {
        this.plugin = plugin;
    }

    MiniMessage minimessage = MiniMessage.builder()
            .tags(TagResolver.builder()
                    .resolver(StandardTags.color())
                    .resolver(StandardTags.decorations())
                    .resolver(StandardTags.gradient())
                    .resolver(StandardTags.rainbow())
                    .build()
            )
            .build();

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        this.processChunk(event.getChunk());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        this.processChunk(event.getChunk());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.SPAWNER) {
            CreatureSpawner spawner = (CreatureSpawner) block.getState();
            if (plugin.getBlacklistedSpawners().containsKey(spawner.getSpawnedType())) {
                event.setCancelled(true);
                var blacklisted = minimessage.deserialize(Objects.requireNonNull(this.plugin.getConfig().getString("prefix")) + this.plugin.getConfig().getString("blacklisted-block-player-message"));
                event.getPlayer().sendMessage(blacklisted);
            }
        }
    }

    public void processChunk(Chunk chunk) {
        HashMap<EntityType, Boolean> blacklistedSpawners = plugin.getBlacklistedSpawners();
        if (blacklistedSpawners != null) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (BlockState blockState : chunk.getTileEntities()) {
                        if (blockState.getBlock().getType() == Material.SPAWNER) {
                            CreatureSpawner spawner = (CreatureSpawner) blockState;
                            if (blacklistedSpawners.containsKey(spawner.getSpawnedType()) && blacklistedSpawners.get(spawner.getSpawnedType())) {
                                spawner.getBlock().setType(Material.AIR);
                                spawner.setDelay(1);
                                spawner.setMinSpawnDelay(1);
                                spawner.setMaxSpawnDelay(2);
                                spawner.setSpawnCount(0);
                                spawner.setMaxNearbyEntities(0);
                                spawner.setRequiredPlayerRange(0);
                            }
                        }
                    }
                }
            }
        }
    }
}
