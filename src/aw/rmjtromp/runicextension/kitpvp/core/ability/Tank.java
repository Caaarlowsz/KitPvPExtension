package aw.rmjtromp.runicextension.kitpvp.core.ability;

import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import aw.rmjtromp.runicextension.kitpvp.core.KitPlayer;

public final class Tank extends Ability {

	public Tank() {
		super(3);
	}
	
	@Override
	public boolean onAbility(KitPlayer player) {
		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 3*20, 1, true, true));
		player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 3*20, 0, true, true));
		player.playSound(player.getEyeLocation(), Sound.FIZZ, 1, 1);
		return true;
	}

}
