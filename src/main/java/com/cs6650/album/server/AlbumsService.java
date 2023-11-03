package com.cs6650.album.server;




import com.cs6650.album.server.bean.AlbumInfo;
import com.cs6650.album.server.bean.ImageMetaData;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AlbumsService {
//    private ConcurrentHashMap<String, AlbumInfo> albumInfos = new ConcurrentHashMap<>();
    private static final Logger logger = Logger.getLogger(AlbumsService.class);
//    AtomicInteger albumId = new AtomicInteger(1);
    private AlbumsDatabase albumsDatabase = new AlbumsDatabase();


    public AlbumInfo doGetAlbumByKey(String albumIdStr) throws IOException {
//        AlbumInfo album = albumInfos.get(albumIdStr);
//        if (album == null) {
//            throw new IOException("Album not found for the given id: " + albumIdStr);
//        }
//        return album;
        try {
            int albumId = Integer.parseInt(albumIdStr);
            return albumsDatabase.getAlbumInfoById(albumId);
        } catch (Exception e) {
            logger.error("Failed to fetch album info from database.", e);
            throw new IOException("Database error while fetching album info.", e);
        }
    }


    public String doPostNewAlbum(AlbumInfo albumInfo) throws IOException {
//        String newAlbumId = String.valueOf(albumId.getAndIncrement());
////        logger.info("New album id: " + newAlbumId);
//
////        String newAlbumId = UUID.randomUUID().toString();
//        albumInfos.put(newAlbumId, albumInfo);
//        return newAlbumId;
        try {
            int newAlbumId = albumsDatabase.insertNewAlbum(albumInfo);
            return String.valueOf(newAlbumId);
        } catch (SQLException e) {
            logger.error("Failed to insert new album into database.", e);
            throw new IOException("Database error while inserting new album.", e);
        }
    }

}

