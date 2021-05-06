package ac.grim.grimac.events.packets;

import ac.grim.grimac.GrimAC;
import io.github.retrooper.packetevents.event.PacketListenerDynamic;
import io.github.retrooper.packetevents.event.impl.PacketPlaySendEvent;
import io.github.retrooper.packetevents.event.priority.PacketEventPriority;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.play.out.entityvelocity.WrappedPacketOutEntityVelocity;
import io.github.retrooper.packetevents.packetwrappers.play.out.explosion.WrappedPacketOutExplosion;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class PacketPlayerVelocity extends PacketListenerDynamic {
    public PacketPlayerVelocity() {
        super(PacketEventPriority.MONITOR);
    }

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        byte packetID = event.getPacketId();
        if (packetID == PacketType.Play.Server.ENTITY_VELOCITY) {
            WrappedPacketOutEntityVelocity velocity = new WrappedPacketOutEntityVelocity(event.getNMSPacket());
            Entity entity = velocity.getEntity();
            if (entity != null) {
                if (entity.equals(event.getPlayer())) {
                    double velX = velocity.getVelocityX();
                    double velY = velocity.getVelocityY();
                    double velZ = velocity.getVelocityZ();

                    Vector playerVelocity = new Vector(velX, velY, velZ);
                    //Bukkit.broadcastMessage("Adding " + playerVelocity);

                    GrimAC.playerGrimHashMap.get(event.getPlayer()).compensatedKnockback.addPlayerKnockback(playerVelocity);
                }
            }
        }

        if (packetID == PacketType.Play.Server.EXPLOSION) {
            WrappedPacketOutExplosion explosion = new WrappedPacketOutExplosion(event.getNMSPacket());

            double x = explosion.getPlayerMotionX();
            double y = explosion.getPlayerMotionY();
            double z = explosion.getPlayerMotionZ();

            // Don't get GrimPlayer object if we don't have to
            if (x != 0 || y != 0 || z != 0) {
                GrimAC.playerGrimHashMap.get(event.getPlayer()).compensatedExplosion.addPlayerExplosion(x, y, z);
            }
        }
    }
}
