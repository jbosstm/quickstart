#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <string>
#include <map>

#include "HttpClient.h"
 
// g++ HttpClient.cxx mongoose.c t.c -pthread -ldl -o w

static HttpClient _wc;

const char *qname = "http://localhost:8080/bt-messaging-5.0.0.Final-SNAPSHOT/queues/jms.queue.testQueue";

static const char* PULL_CONSUMERS = "msg-pull-consumers";
static const char* CREATE_WITH_ID = "msg-create-with-id";
static const char* CREATE_HDR = "msg-create";
static const char* PUSH_CONSUMERS = "msg-push-consumers";

static const char* CREATE_NEXT = "msg-create-next";
static const char* PULL_SUBSCRIPTIONS = "msg-pull-subscriptions";
static const char* CONSUME_NEXT = "msg-consume-next";

static const char* CONSUMER  = "msg-consumer";

typedef std::map<std::string, std::string> HeaderMap;

const HeaderMap::value_type msg_headers[] = {
	HeaderMap::value_type(PULL_CONSUMERS, ""),
	HeaderMap::value_type(CREATE_WITH_ID, ""),
	HeaderMap::value_type(CREATE_HDR, ""),
	HeaderMap::value_type(PUSH_CONSUMERS, ""),

	HeaderMap::value_type(CREATE_NEXT, ""),
	HeaderMap::value_type(PULL_SUBSCRIPTIONS, ""),
	HeaderMap::value_type(CONSUME_NEXT, ""),
};

static const int nelems = sizeof msg_headers / sizeof msg_headers[0];
static HeaderMap HEADERS(msg_headers, msg_headers + nelems);

static void dump_headers() {
	for (HeaderMap::iterator i = HEADERS.begin(); i != HEADERS.end(); ++i)
		printf("Header: %s=%s\n", i->first.c_str(), i->second.c_str());
}

static void decode_headers(struct mg_request_info* ri) {
	for (int i = 0; i < ri->num_headers; i++)
		HEADERS[ri->http_headers[i].name] = ri->http_headers[i].value;
}

static int qinfo(const char *qname) {
	struct mg_request_info ri;
	char *resp = _wc.send(&ri, "HEAD", qname, "*/*", NULL, NULL);

    if (resp) free(resp);

	decode_headers(&ri);

	return ri.status_code;
}

static int remove_consumer() {
	struct mg_request_info ri;
	std::string consumer = HEADERS[CONSUMER];
	char *resp = _wc.send(&ri, "DELETE", consumer.c_str(), "*/*", NULL, NULL);

//	decode_headers(&ri);
//
    if (resp) free(resp);

	return ri.status_code;
}

static int create_consumer(bool autoack) {
	struct mg_request_info ri;
	const char *body = autoack ? NULL : "autoAck=false";
	const char* mt = "application/x-www-form-urlencoded";
	char *resp;
	std::string consumer = HEADERS[CONSUMER];

	if (consumer.size() > 0)
		remove_consumer();

	consumer = HEADERS[PULL_CONSUMERS];
	resp = _wc.send(&ri, "POST", consumer.c_str(), mt, body, NULL);
	decode_headers(&ri);

    if (resp) free(resp);

	return ri.status_code;
}

static char* try_get_message(struct mg_request_info* ri) {
	std::string nm = HEADERS[CONSUME_NEXT];
	char *resp = _wc.send(ri, "POST", nm.c_str(), "application/x-www-form-urlencoded", NULL, NULL);

	if (ri->status_code == 200 || ri->status_code == 412)
		decode_headers(ri);

	return resp;
}

static void get_message() {
	struct mg_request_info ri;
	char * resp = try_get_message(&ri);

	printf("%d ", ri.status_code);
	if (ri.status_code == 412) {
		// the server has crashed and is passing us a new mesg-consume-next header
		resp = try_get_message(&ri);
		//dump_headers();

		// or the consumer session may have timed out
		if (ri.status_code == 412) {
			printf("recreating consumer and retrying get\n");
			int sc = remove_consumer();

			sc = create_consumer(true);
			resp = try_get_message(&ri);
		}
	} else if (ri.status_code == 503) {
		printf("No messages, try again later\n");
	} else if (ri.status_code != 200) {
		fprintf(stderr, "get status: %d\n", ri.status_code);
	}

	if (ri.status_code == 200) {
		printf("Message: %s\n", resp);
//		printf("next msg: %s\n", HEADERS[CONSUME_NEXT].c_str());
	}

    if (resp) free(resp);
}

static void put_message(const char *msg) {
	struct mg_request_info ri;
	std::string nm = HEADERS[CREATE_HDR];
	char *resp;

	int cnt = 0, i;
	int n = sscanf(msg + 1, "%d %d", &cnt, &i);

	if (cnt <= 0) {
		resp = _wc.send(&ri, "POST", nm.c_str(), "*/*", msg, NULL);
		if (ri.status_code == 201)
			decode_headers(&ri);
	} else {
		while (cnt--) {
			char m[32];
			sprintf(m, "MSG %d", i++);
			printf("%s\n", m);
			resp = _wc.send(&ri, "POST", nm.c_str(), "*/*", m, NULL);
			if (ri.status_code == 201)
				decode_headers(&ri);
			else
				printf("POST status: %d\n", ri.status_code);
		}
	}
}

static int process() {
    char *line = NULL;
    size_t len = 0;
    ssize_t read;

    while ((read = getline(&line, &len, stdin)) != -1) {
        //printf("Retrieved line of length %zu :\n", read); printf("%s", line);
		if (line[0] == 'g')
	 		get_message();
		else if (line[0] == 'p')
			put_message(line);
		else if (line[0] == 'r')
			create_consumer(true);
		else if (line[0] == 'q')
			break;
    }

    free(line);
}

int main(int argc, char* argv[])
{
	int sc = qinfo(qname);

//	printf("qinfo: %d\n", sc);
	printf("syntax: %s 1 (do not start a consumer)\n", argv[0]);
	printf("syntax: %s (starts a consumer)\n", argv[0]);

	if (argc == 1) {
		sc = create_consumer(true);
		printf("create_consumer: %d\n", sc);
	}

//	dump_headers();

	process();

	sc = remove_consumer();
	printf("remove_consumer: %d\n", sc);

	return 0;
}
