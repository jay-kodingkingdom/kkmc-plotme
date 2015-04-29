package com.worldcretornica.plotme;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.worldcretornica.plotme.utils.NameFetcher;

public class PlayerList {

    private HashMap<String, UUID> playerlist;
    
    public PlayerList() {
        playerlist = new HashMap<>();
    }
    
    public PlayerList(HashMap<String, UUID> players) {
        playerlist = players;
    }
    
    public void put(String name) {
        put(name, null);
    }
    
    public void put(String name, UUID uuid) {
        playerlist.put(name, uuid);
    }
    
    public String put(UUID uuid) {
        String name = getPlayerName(uuid);
        playerlist.put(name, uuid);
        return name;
    }
        
    public UUID remove(String name) {
        String found = "";
        UUID uuid = null;
        for(String key : playerlist.keySet())
        {
            if(key.equalsIgnoreCase(name)) {
                found = key;
                continue;
            }
        }
        if(!found.equals("")) {
            uuid = playerlist.get(found);
            playerlist.remove(found);
        }
        return uuid;
    }
    
    public String remove(UUID uuid) {
        for(String name : playerlist.keySet()) {
            if(playerlist.get(name).equals(uuid)) {
                playerlist.remove(name);
                return name;
            }
        }
        return "";
    }
    
    public Set<String> getPlayers() {
        return playerlist.keySet();
    }
    
    public String getPlayerList() {
        StringBuilder list = new StringBuilder();

        for (String s : playerlist.keySet()) {
                list = list.append(s + ", ");
        }
        if (list.length() > 1) {
            list = list.delete(list.length() - 2, list.length());
        }
        if(list.toString() == null)
        {
            return "";
        } else {
            return list.toString();
        }
    }
    
    public boolean contains(String name) {
        for(String key : playerlist.keySet()) {
            if(key.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean contains(UUID uuid) {
        return playerlist.values().contains(uuid);
    }
    
    public HashMap<String, UUID> getAllPlayers() {
        return playerlist;
    }
    
    public void clear() {
        playerlist.clear();
    }
    
    public int size() {
        return playerlist.size();
    }
    
    public void replace(UUID uuid, String newname) {
        if(uuid != null && playerlist != null) {
            if(this.contains(uuid)) {
                Iterator<String> it = playerlist.keySet().iterator();
                while (it.hasNext()) {
                    String name = it.next();
                    
                    if(playerlist.get(name) != null && playerlist.get(name).equals(uuid)) {
                        playerlist.remove(name);
                        playerlist.put(newname, uuid);
                        return;
                    }
                }
            }
        }
    }
    
    public void replace(String name, UUID newuuid) {
        if(newuuid != null && playerlist != null) {
            if(this.contains(name)) {
                Iterator<String> it = playerlist.keySet().iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    
                    if(key.equalsIgnoreCase(name)) {
                        playerlist.remove(key);
                        playerlist.put(name, newuuid);
                        return;
                    }
                }
            }
        }
    }
    

	private static final long bufferTime = 2*1000;
	private static ReentrantLock lock = new ReentrantLock(); 
	private static String getPlayerName(UUID playerId){
		OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);

        if (player.getName() != null) {
            return player.getName();}
        else {
        	lock.lock();
    		try {
    			Thread.sleep(bufferTime);}
    		catch (InterruptedException e) {}
    		finally{
    			lock.unlock();}
    			
            List<UUID> names = new ArrayList<UUID>();
            names.add(playerId);
            NameFetcher fetcher = new NameFetcher(names);

            try {
            	PlotMe.logger.info("Fetching " + playerId.toString() + " Name from Mojang servers...");
                Map<UUID, String> response = fetcher.call();
                
                if (response.size() > 0) {
                	String playerName = response.values().toArray(new String[0])[0];
                	PlotMe.logger.info("Fetched " + playerName + " for " + playerId.toString());
                	return playerName;}}
            catch (IOException e) {
            	PlotMe.logger.info("Unable to connect to Mojang server!");
            	if (e.getMessage()!=null&&e.getMessage().contains("HTTP response code: 429")){
            		PlotMe.logger.info("HTTP response code 429");
            		PlotMe.logger.info("Retrying...");
            		return getPlayerName(playerId);}} 
            catch (Exception e) {
            	PlotMe.logger.info("Exception while running NameFetcher");
                e.printStackTrace();}}
		return null;}
    
}
