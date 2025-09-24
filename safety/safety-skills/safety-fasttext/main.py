import logging
import os

from flask import Flask, request

import app.model.models as models
import app.utils.log as log

logger = logging.getLogger(__name__)

pycharm_debug = os.environ.get('PYCHARM_DEBUG', '0')
print(f'PYCHARM_DEBUG={pycharm_debug}')
if pycharm_debug == '1':
    print(f'IN IDE DEBUG')
    log.init('debug')
else:
    print(f'IN PRODUCTION')
    log.init('prod')

def create_app():
    app = Flask(__name__)
    if 'FASTTEXT_CONFIG' not in os.environ:
        raise Exception('FASTTEXT_CONFIG not set')
    config_file = os.environ['FASTTEXT_CONFIG']
    models.init(config_file)
    return app

app = create_app()
@app.route("/health")
def health():
    return {
        'status': 'ok'
    }

@app.after_request
def after_request(response):
    """Add Version headers to the response."""
    return response


@app.before_request
def before_request():
    try:
        method = request.method
        if method == 'POST':
            body = request.get_json(cache=True)
            logging.info(f'url={request.url}, method={method}, body={body}')
        else:
            logging.info(f'url={request.url}, method={method}')

    except Exception as e:
        logging.warning(f'url={request.url}, method={request.method}, error={str(e)}')

@app.route('/fasttext/<model_name>', methods=['POST'])
def handle_fasttext(model_name: str):
    req = request.get_json()
    return models.classify_by_model_name(req, model_name)


if __name__ == "__main__":
    app.debug = True
    app.run(
        host='0.0.0.0',
        port=8002
    )