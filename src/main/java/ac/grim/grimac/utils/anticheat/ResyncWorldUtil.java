package ac.grim.grimac.utils.anticheat;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.blockstate.FlatBlockState;
import ac.grim.grimac.utils.blockstate.MagicBlockState;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.grim.grimac.utils.data.BasePlayerChangeBlockData;
import ac.grim.grimac.utils.data.PlayerOpenBlockData;
import ac.grim.grimac.utils.math.GrimMathHelper;
import io.github.retrooper.packetevents.utils.server.ServerVersion;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;

@UtilityClass
public class ResyncWorldUtil {
    public void resyncPositions(GrimPlayer player, SimpleCollisionBox box) {
        resyncPositions(player, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
    }

    public void resyncPositions(GrimPlayer player, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        resyncPositions(player, GrimMathHelper.floor(minX), GrimMathHelper.floor(minY), GrimMathHelper.floor(minZ),
                GrimMathHelper.floor(maxX), GrimMathHelper.floor(maxY), GrimMathHelper.floor(maxZ));
    }

    public void resyncPositions(GrimPlayer player, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        int[][][] blocks = new int[maxX - minX + 1][maxY - minY + 1][maxZ - minZ + 1];

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    blocks[x - minX][y - minY][z - minZ] = player.compensatedWorld.getWrappedBlockStateAt(x, y, z).getCombinedId();
                }
            }
        }

        for (BasePlayerChangeBlockData changeBlockData : player.compensatedWorld.worldChangedBlockQueue) {
            if (changeBlockData instanceof PlayerOpenBlockData) continue; // Server will resync this later
            if (changeBlockData.blockX >= minX && changeBlockData.blockX <= maxX &&
                    changeBlockData.blockY >= minY && changeBlockData.blockY <= maxY &&
                    changeBlockData.blockZ >= minZ && changeBlockData.blockZ <= maxZ) { // in range
                // Update this block data to latest to stop any desync's
                blocks[changeBlockData.blockX - minX][changeBlockData.blockY - minY][changeBlockData.blockZ - minZ] = changeBlockData.getCombinedID();
            }
        }

        // Maybe in the future chunk changes could be sent, but those have a decent amount of version differences
        // Works for now, maybe will fix later, maybe won't.
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (ServerVersion.getVersion().isNewerThanOrEquals(ServerVersion.v_1_13)) {
                        FlatBlockState state = new FlatBlockState(blocks[x - minX][y - minY][z - minZ]);
                        player.bukkitPlayer.sendBlockChange(new Location(player.bukkitPlayer.getWorld(), x, y, z), state.getBlockData());
                    } else {
                        MagicBlockState state = new MagicBlockState(blocks[x - minX][y - minY][z - minZ]);
                        player.bukkitPlayer.sendBlockChange(new Location(player.bukkitPlayer.getWorld(), x, y, z), state.getMaterial(), (byte) state.getBlockData());
                    }
                }
            }
        }
    }
}
