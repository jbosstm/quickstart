#include <stdio.h>
#include <string.h>

#ifdef DB2
#include <sqlcli.h>
#include "xa.h"
 
#include "tx/request.h"

#ifdef DECLSPEC_DEFN
extern __declspec(dllimport) struct xa_switch_t db2xa_switch_std;
extern __declspec(dllimport) struct xa_switch_t * SQL_API_FN db2xacic_std();
#else
#define db2xa_switch_std (*db2xa_switch_std)
extern struct xa_switch_t db2xa_switch_std;
struct xa_switch_t * SQL_API_FN db2xacic_std();
#endif

static SQLCHAR CTSQL[] = "CREATE TABLE XEMP (EMPNO integer NOT NULL PRIMARY KEY, ENAME varchar(32))";
/*static SQLCHAR DTSQL[] = "DROP TABLE XEMP";*/
static SQLCHAR ISQL[] = "INSERT INTO XEMP VALUES (?, 'Jim')";
static SQLCHAR USQL[] = "UPDATE XEMP SET ENAME='NEW_NAME' WHERE EMPNO=?";
static SQLCHAR DSQL[] = "DELETE FROM XEMP WHERE EMPNO >= ?";
static SQLCHAR SSQL[] = "select EMPNO, ENAME from XEMP WHERE EMPNO >= ?";

static SQLRETURN check_error(SQLSMALLINT, SQLHANDLE, SQLRETURN, const char*, SQLCHAR*);
#define CHECK_HANDLE( htype, hndl, rc, msg, sql ) \
   if (rc != SQL_SUCCESS) {return check_error(htype,hndl,rc,msg,sql);}

long db2_xaflags()
{
	struct xa_switch_t *xasw = db2xacic_std();

	return xasw->flags; //db2xa_switch_std.flags;
}

/* can't get SQLBindParameter to work - use string substitution for the time being */
static const char *strsub(const char *src, char *dest, size_t sz, const char *match, char *rep)
{
	char *s;
	size_t len;

	if ((s = strstr(src, match)) == NULL)
		return NULL;

	len = s - src;  /* length of the string upto the match */
	strncpy(dest, src, len);
	sprintf(dest+len, "%s%s", rep, s + strlen(match));

	return dest;
}

static SQLRETURN doSelect(SQLHENV henv, SQLHDBC hdbc, SQLHSTMT shdl, SQLCHAR sql[], int *rcnt)
{
	SQLCHAR name[33];
	SQLINTEGER empno, col2sz, col1sz;
	SQLRETURN rc = SQLExecDirect(shdl, sql, SQL_NTS);

	CHECK_HANDLE(SQL_HANDLE_STMT, shdl, rc, "SQLExecDirect", sql);
 
	/* bind empno and name to columns 1 and 2 of the fetch */
	SQLBindCol(shdl, 1, SQL_C_LONG, (SQLPOINTER) &empno, (SQLINTEGER) sizeof (SQLINTEGER), (SQLINTEGER *) &col1sz);
	SQLBindCol(shdl, 2, SQL_C_CHAR, (SQLPOINTER) name, (SQLINTEGER) sizeof (name), &col2sz);
 
	*rcnt = 0;
	while (SQLFetch(shdl) == SQL_SUCCESS) {
		*rcnt += 1;
/*		btlogger_debug("(%ld,%s)\n", empno, name);*/
	}

	return SQL_SUCCESS;
}

static SQLRETURN doSql(SQLHENV henv, SQLHDBC hdbc, SQLHSTMT shdl, SQLCHAR sql[])
{
	SQLRETURN rc;

	btlogger_debug("doSql %s\n", sql);
	rc = SQLPrepare(shdl, sql, SQL_NTS);
	CHECK_HANDLE(SQL_HANDLE_STMT, shdl, rc, "SQLPrepare", sql);
	
	SQLExecDirect(shdl, sql, SQL_NTS);
	CHECK_HANDLE(SQL_HANDLE_STMT, shdl, rc, "SQLExecDirect", sql);

	return SQL_SUCCESS;
}

static SQLRETURN fini(SQLHENV henv, SQLHDBC hdbc)
{
	/* clean up - free the connection and environment handles */
	SQLDisconnect(hdbc);
	SQLFreeConnect(hdbc);
	SQLFreeEnv(henv);

	return SQL_SUCCESS;
}

static SQLRETURN init(char *dbalias, SQLHENV *henv, SQLHDBC *hdbc)
{
	SQLHSTMT hstmt;	/* statement handle */
	SQLRETURN rc;

	*henv = 0;
	rc = SQLAllocEnv(henv);
	CHECK_HANDLE(SQL_HANDLE_ENV, *henv, rc, "SQLAllocEnv", (SQLCHAR *) "");

	rc = SQLAllocConnect(*henv, hdbc);
	CHECK_HANDLE(SQL_HANDLE_DBC, *hdbc, rc, "SQLAllocConnect", (SQLCHAR *) "");

	rc = SQLConnect(*hdbc, (SQLCHAR *) dbalias, SQL_NTS, NULL, SQL_NTS, NULL, SQL_NTS);
	CHECK_HANDLE(SQL_HANDLE_DBC, *hdbc, rc, "SQLConnect", (SQLCHAR *) "");

	rc = SQLAllocHandle(SQL_HANDLE_STMT, *hdbc, &hstmt);
	CHECK_HANDLE(SQL_HANDLE_STMT, hstmt, rc, "SQLAllocHandle", (SQLCHAR *) "");

	(void) SQLExecDirect(hstmt, CTSQL, SQL_NTS);
	/*CHECK_HANDLE(SQL_HANDLE_STMT, hstmt, rc, "SQLExecDirect", CTSQL);*/

	(void) SQLFreeStmt(hstmt, SQL_DROP);

	return rc;
}

static SQLRETURN doWork(char op, char *arg, SQLHENV henv, SQLHDBC hdbc, SQLHSTMT shdl, test_req_t *resp) 
{
	SQLRETURN status = SQL_ERROR;
	int empno = (*arg ? atoi(arg) : 8000);
	char buf1[512];
	char buf2[512];
	int rcnt = 0;   // no of matching records

	sprintf(buf2, "%d", empno);
	(resp->data)[0] = 0;

	if (op == '0') {
		(void) strsub((const char*) ISQL, buf1, sizeof (buf1), "?", buf2);
		status = doSql(henv, hdbc, shdl, (SQLCHAR *) buf1);
	} else if (op == '1') {
		(void) strsub((const char*) SSQL, buf1, sizeof (buf1), "?", buf2);
		status = doSelect(henv, hdbc, shdl, (SQLCHAR *) buf1, &rcnt);
		btlogger_snprintf(resp->data, sizeof (resp->data), "%d", rcnt);
	} else if (op == '2') {
		(void) strsub((const char*) USQL, buf1, sizeof (buf1), "?", buf2);
		status = doSql(henv, hdbc, shdl, (SQLCHAR *) buf1);
	} else if (op == '3') {
		(void) strsub((const char*) DSQL, buf1, sizeof (buf1), "?", buf2);
		status = doSql(henv, hdbc, shdl, (SQLCHAR *) buf1);
	}

	return status;
}

int db2_access(test_req_t *req, test_req_t *resp)
{
	SQLHENV henv;	/* environment handle */
	SQLHDBC hdbc;	/* connection handle */
	SQLRETURN rc = SQL_ERROR;
	SQLHSTMT shdl;

	btlogger_debug("op=%c data=%s db=%s\n", req->op, req->data, req->db);

	if (init(req->db, &henv, &hdbc) != SQL_SUCCESS)
		return (int) rc;

	rc = SQLAllocHandle(SQL_HANDLE_STMT, hdbc, &shdl);

	if ( rc != SQL_SUCCESS ) {
		(void) check_error(SQL_HANDLE_STMT, shdl, rc, "SQLAllocHandle", (SQLCHAR *) "statement handle");
	} else {
		rc = doWork(req->op, req->data, henv, hdbc, shdl, resp);
		(void) SQLFreeStmt(shdl, SQL_DROP);
	}

	fini(henv, hdbc);

	return (int) rc;
}

/**
 * print diagnostics in case of error:
 * convert error code from last CLI invokation to a string
 * and use SQLGetDiagRec() to display the SQLSTATE and message
 * for the given handle type
 */
static SQLRETURN check_error( SQLSMALLINT htype, /* A handle type */
					   SQLHANDLE   hndl,  /* A handle */
					   SQLRETURN   rc,   /* Return code */
					   const char* msg,   /* prefix message */
					   SQLCHAR*	sql)   /* the sql for statement handles */
{
	SQLCHAR	 buffer[SQL_MAX_MESSAGE_LENGTH + 1] ;
	SQLCHAR	 sqlstate[SQL_SQLSTATE_SIZE + 1] ;
	SQLINTEGER  sqlcode ;
	SQLSMALLINT length, i = 1;

	switch (rc) {
	case SQL_SUCCESS:	/* 0 */
		return rc;
	case SQL_SUCCESS_WITH_INFO:	/* 1 */
		btlogger_warn("%s error:  SQL_SUCCESS_WITH_INFO: ...", msg);
		break;
	case SQL_NO_DATA_FOUND:	/* 100 */
		btlogger_warn("%s error: SQL_NO_DATA_FOUND: ...", msg);
		break;
	case SQL_ERROR:	/* -1 */
		btlogger_warn("%s error: SQL_ERROR: ...", msg);
		break;
	case SQL_INVALID_HANDLE:	/* -2 */
		btlogger_warn("%s error: SQL_INVALID HANDLE: ...", msg);
		break;
	default:
		/* SQL_NEED_DATA, SQL_NO_DATA, SQL_STILL_EXECUTING */
		btlogger_warn("%s error: code=%d: ...", msg, rc);
		break;
	}

	/*
	 * use SQLGetDiagRec() to display SQLSTATE and message for the given handle type
	 */
	while (SQLGetDiagRec(htype, hndl, i++, sqlstate, &sqlcode, buffer,
		SQL_MAX_MESSAGE_LENGTH + 1, &length) == SQL_SUCCESS ) {
	   btlogger_warn( "\t*** SQLSTATE: %s", sqlstate) ;
	   btlogger_warn( "\t*** Native Error Code: %ld", sqlcode) ;
	   btlogger_warn( "\t*** buffer: %s", buffer) ;
	}

	return rc;
}
#endif
