#include "xatmi.h"

struct message_t {
	char reply_to[XATMI_SERVICE_NAME_LENGTH];
    char data[10];
};
typedef struct message_t MESSAGE;

