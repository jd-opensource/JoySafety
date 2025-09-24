import json

from openai import OpenAI
from openai.types.chat import ChatCompletion, ChatCompletionMessage
from openai.types.chat.chat_completion import Choice

import logging
logger = logging.getLogger(__name__)

def invoke_llm(client: OpenAI, messages, config: dict) -> str:
    ai_resp = None
    chat_completion: ChatCompletion = client.chat.completions.create(
        messages=messages,
        **config
    )
    logger.info(f"message={json.dumps(messages, ensure_ascii=False)},"
                f" resp={json.dumps(chat_completion.model_dump(), ensure_ascii=False)}")
    if not chat_completion.choices:
        logger.warning(f"choices为空")
        return ai_resp
    choice: Choice = chat_completion.choices[0]
    if not choice.message:
        logger.warning(f"message为空")
        return ai_resp
    message: ChatCompletionMessage = choice.message
    if not message.content:
        logger.warning(f"content为空")
        return ai_resp
    else:
        return message.content