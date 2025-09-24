import importlib.util
import sys
from app.conf import Conf

def load_config(path: str, module_name: str = "kg_config"):
    spec = importlib.util.spec_from_file_location(module_name, path)
    module = importlib.util.module_from_spec(spec)
    sys.modules[module_name] = module
    spec.loader.exec_module(module)
    return module



if __name__ == '__main__':
    conf = load_config('../../docs/config_example.py')
    print(conf.config)

    conf_obj = Conf(**conf.config)
    print(conf_obj)