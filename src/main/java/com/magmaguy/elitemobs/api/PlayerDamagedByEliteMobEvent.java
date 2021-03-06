package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.utils.EventCaller;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.concurrent.ThreadLocalRandom;

public class PlayerDamagedByEliteMobEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Entity entity;
    private final EliteMobEntity eliteMobEntity;
    private final Player player;
    private boolean isCancelled = false;
    private final EntityDamageByEntityEvent entityDamageByEntityEvent;
    private final Projectile projectile;

    public PlayerDamagedByEliteMobEvent(EliteMobEntity eliteMobEntity, Player player, EntityDamageByEntityEvent event, Projectile projectile) {
        this.entity = event.getEntity();
        this.eliteMobEntity = eliteMobEntity;
        this.player = player;
        this.entityDamageByEntityEvent = event;
        this.projectile = projectile;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public EliteMobEntity getEliteMobEntity() {
        return this.eliteMobEntity;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Projectile getProjectile() {
        return projectile;
    }

    public EntityDamageByEntityEvent getEntityDamageByEntityEvent() {
        return this.entityDamageByEntityEvent;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
        entityDamageByEntityEvent.setCancelled(b);
    }

    public static class PlayerDamagedByEliteMobEventFilter implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void onEliteMobAttack2(EntityDamageByEntityEvent event) {
            if (!(event.getEntity() instanceof Player)) return;
            Player player = (Player) event.getEntity();

            //citizens
            if (player.hasMetadata("NPC"))
                return;

            Projectile projectile = null;

            EliteMobEntity eliteMobEntity = null;
            if (event.getDamager() instanceof LivingEntity)
                eliteMobEntity = EntityTracker.getEliteMobEntity(event.getDamager());
            else if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof LivingEntity) {
                eliteMobEntity = EntityTracker.getEliteMobEntity((LivingEntity) ((Projectile) event.getDamager()).getShooter());
                projectile = (Projectile) event.getDamager();
            }if (eliteMobEntity == null) return;

            //dodge chance
            if (ThreadLocalRandom.current().nextDouble() < GuildRank.dodgeBonusValue(GuildRank.getGuildPrestigeRank(player), GuildRank.getActiveGuildRank(player)) / 100) {
                player.sendTitle("", "Dodged!");
                event.setCancelled(true);
                return;
            }

            PlayerDamagedByEliteMobEvent playerDamagedByEliteMobEvent = new PlayerDamagedByEliteMobEvent(eliteMobEntity, player, event, projectile);
            if (!playerDamagedByEliteMobEvent.isCancelled)
                new EventCaller(playerDamagedByEliteMobEvent);
        }
    }

}
