package com.bsi.md.agent.access;

import com.healthmarketscience.jackcess.CryptCodecProvider;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import net.ucanaccess.jdbc.JackcessOpenerInterface;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class JackcessOpener implements JackcessOpenerInterface {
    @Override
    public Database open(File file, String s) throws IOException {
        DatabaseBuilder builder = new DatabaseBuilder(file);
        builder.setCharset(Charset.forName("gbk"));
        builder.setAutoSync(false);
        builder.setCodecProvider(new CryptCodecProvider(s));
        builder.setReadOnly(false);
        return builder.open();
    }
}