package com.developmentontheedge.be5.server.services.rememberme;

import com.developmentontheedge.be5.database.DbService;

import javax.inject.Inject;
import java.sql.Timestamp;

public class PersistentTokenRepositoryImpl implements PersistentTokenRepository
{
    private final DbService db;

    @Inject
    public PersistentTokenRepositoryImpl(DbService db)
    {
        this.db = db;
    }

    public void createNewToken(PersistentRememberMeToken token)
    {
        db.update("insert into persistent_logins (user_name, series, token, last_used) values(?,?,?,?)",
                token.getUsername(), token.getSeries(), token.getTokenValue(), token.getTimestamp());
    }

    public void updateToken(String series, String tokenValue, Timestamp lastUsed)
    {
        db.update("update persistent_logins set token = ?, last_used = ? where series = ?", tokenValue, lastUsed, series);
    }

    public PersistentRememberMeToken getTokenForSeries(String seriesId)
    {
        return db.select("select user_name,series,token,last_used from persistent_logins where series = ?",
                rs -> new PersistentRememberMeToken(rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getTimestamp(4)), seriesId);
    }

    public void removeUserTokens(String username)
    {
        db.update("delete from persistent_logins where user_name = ?", username);
    }
}
