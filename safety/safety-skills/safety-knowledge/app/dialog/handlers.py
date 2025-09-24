from typing import List

from openai import OpenAI

from app.base import *
from app.conf import DialogCheckConf
from app.dialog.prompt import *
from app.util.llm_util import invoke_llm

logger = logging.getLogger(__name__)

class Message(BaseModel):
    role: str
    content: str


class DialogCheckRequest(Request):
    history: List[Message]
    rate_limit: bool = True


class DialogCheckResponse(BaseModel):
    riskCode: int = 0
    riskMessage: str = '正常文本'
    reason: str = ''

prompt_principle = PROMPT_DEFAULT_PRINCIPLE
def build_prompt(history: List[Message]) -> str:
    messages = [{"role": h.role, "content": h.content} for h in history]
    ret = PROMPT_PREFIX + prompt_principle + PROMPT_DEFAULT_SUFFIX.format(messages=messages)
    return ret


class LlmResp(BaseModel):
    label: int
    reason: str

import re
pattern = re.compile(r"({.*})")


client: OpenAI = None
openai_config = {}
# https://platform.openai.com/docs/libraries
# https://platform.openai.com/docs/api-reference/chat
def init(conf: DialogCheckConf):
    global client, openai_config
    openai_config = conf.openai_config
    client = OpenAI(
        base_url=conf.openai_host,
        api_key=conf.openai_key,
        max_retries=conf.max_retries,
    )

def handle(request: DialogCheckRequest) -> DialogCheckResponse:
    if not request.history:
        raise Exception('history为空')

    prompt: str = build_prompt(request.history)
    messages = [
        {"role": "user", "content": prompt}
    ]

    ret = DialogCheckResponse()
    content = invoke_llm(client, messages, openai_config)
    if not content:
        logger.error(f"无正常回复")
        return ret

    match = pattern.search(content)
    if not match:
        logger.error(f"无正常回复")
        return ret
    else:
        group = match.group()
        resp_dict = json.loads(group)
        resp_body = LlmResp(**resp_dict)
        ret.riskCode = resp_body.label
        ret.reason = resp_body.reason
        if ret.riskCode == 1:
            ret.riskMessage = '恶意对话'
        return ret