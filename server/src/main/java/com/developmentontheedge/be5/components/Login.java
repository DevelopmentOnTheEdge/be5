package com.developmentontheedge.be5.components;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.metadata.SessionConstants;
import com.developmentontheedge.be5.metadata.Utils;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.util.DateUtils;

import javax.servlet.http.HttpSession;
import java.sql.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Login implements Component
{
    private static final Logger log = Logger.getLogger(Login.class.getName());

    public static class State
    {
        
        public final boolean loggedIn;

        public State(boolean loggedIn)
        {
            this.loggedIn = loggedIn;
        }
        
    }
    
    @Override
    public void generate(Request req, Response res, ServiceProvider serviceProvider)
    {
        switch (req.getRequestUri())
        {
        case "":
            login(req, res, serviceProvider);
            return;
        // deprecated, the 'state' method should be instead instead
        case "test":
            res.sendAsRawJson(UserInfoHolder.isLoggedIn());
            return;
        case "state":
            res.sendAsJson("loginState", getState(req, serviceProvider));
            return;
        default:
            res.sendUnknownActionError();
            return;
        }
    }
    
    private State getState(Request req, ServiceProvider serviceProvider)
    {
        return new State(UserInfoHolder.isLoggedIn());
    }
    
    private void login(Request req, Response res, ServiceProvider serviceProvider)
    {
        String username = req.get("username");
        String password = req.get("password");
        
        if (isNullOrEmpty(username) || isNullOrEmpty(password))
        {
            res.sendError("Empty username or password", "loginError");
            return;
        }
        
        if (!serviceProvider.getLoginService().login(req, username, password))
        {
            res.sendError("Access denied", "loginError");
            return;
        }

        //TODO move it to condolk
        if(serviceProvider.getProject().getAppName().equals("condolk"))
        {
            try
            {
                initSessionVars(username, req, serviceProvider);
            } catch (Exception exc)
            {
                res.sendError("Access denied", "loginError");
                throw Be5Exception.internal(exc, "Невозможно установить переменные сессии для пользователя");
            }
        }
        res.sendSuccess();
    }

    private static final String PERSON_ID_SESSION = "personID";
    private static final String PERSON_ID_SESSION_FOR_SERVLETS = SessionConstants.USER_VAR_PREFIX + PERSON_ID_SESSION;
    private static final String WORKING_MONTH_SESSION_SESSION_FOR_SERVLETS = "user-workingMonth";
    private static final String WORKING_DAY_SESSION_SESSION_FOR_SERVLETS = "user-workingDay";

    private void initSessionVars(String username, Request req, ServiceProvider sp)
    {
        SqlService db = sp.get(SqlService.class);
        HttpSession session = req.getRawSession();

        Long personId = db.selectScalar("SELECT id FROM public.persons WHERE username = ?", username);

        if(personId != null)
        {
            session.setAttribute(PERSON_ID_SESSION_FOR_SERVLETS, personId);

            Query minWorkingMonthQuery = sp.getMeta().getQueryIgnoringRoles("_lk_", "minWorkingMonth");
            Date minWorkingMonth = (Date) sp.getExecutorService()
                    .createExecutor(minWorkingMonthQuery, req).getRow().getValue("minWorkingMonth");

            Date resultWorkingDay = getWorkingDay(minWorkingMonth);

            session.setAttribute(WORKING_MONTH_SESSION_SESSION_FOR_SERVLETS, minWorkingMonth);
            session.setAttribute(WORKING_DAY_SESSION_SESSION_FOR_SERVLETS, resultWorkingDay);
        }
        else
        {
            log.log(Level.WARNING, "personId is null");
        }
    }

    private static java.sql.Date getWorkingDay( final java.sql.Date workingMonth )
    {
        java.sql.Date resultWorkingDay = new java.sql.Date(System.currentTimeMillis());
        if( DateUtils.getMonth( resultWorkingDay ) != DateUtils.getMonth( workingMonth ) || DateUtils.getYear( resultWorkingDay ) != DateUtils.getYear( workingMonth ) )
        {
            resultWorkingDay = ( java.sql.Date )DateUtils.curMonthEnd( workingMonth );
        }
        return resultWorkingDay;
    }

}
