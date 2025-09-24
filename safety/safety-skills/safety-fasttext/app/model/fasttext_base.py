import re
import string
from typing import List

import fasttext
import jieba


class FasttextModel(object):
    def __init__(self, model_path, stopword_path, k=1024, label_mapping:dict = {}):
        self.model_path = model_path
        self.stopword_path = stopword_path
        self.model = fasttext.load_model(self.model_path)
        self.stop_words = set(line.strip() for line in open(self.stopword_path, 'r', encoding='utf-8').readlines())
        self.k = k
        for k,v in label_mapping.items():
            if not v or not v[0].isdigit():
                raise Exception(f'label_mapping error, {k}:{v}，value首字母需要是数字')

        self.label_mapping = label_mapping
    # 文本清洗
    def clean_text(self, text):
        cleaned = re.sub(r"[%s%s]+" % (string.punctuation, '。，、；：？！“”‘’（）{}【】《》…—'), "", text)  # 去除中文和英文标点符号
        return cleaned

    # 中文分词
    def tokenize_cn(self, text):
        return list(jieba.cut(text))

    # 去除停用词
    def remove_stopwords(self, words):
        return [word for word in words if word not in self.stop_words]

    def inference_one(self, input_text):
        cleaned_text = self.clean_text(input_text)
        tokenized_text = self.tokenize_cn(cleaned_text)
        filtered_text = self.remove_stopwords(tokenized_text)
        processed_text = ' '.join(filtered_text)
        processed_text = processed_text.replace('\n', ' ').replace('\r', ' ')
        # 注意这里的k参数，它决定了返回多少个预测结果
        predict_labels, probabilities = self.model.predict(processed_text, k=self.k)
        predict_labels = [item for item in predict_labels]
        if self.label_mapping:
            for i in range(len(predict_labels)):
                if predict_labels[i] in self.label_mapping:
                    predict_labels[i] = self.label_mapping[predict_labels[i]]
        # 将所有标签和概率整合到一起返回
        results = dict(zip(predict_labels, probabilities))
        detail = []
        pattern = r"\d+"
        max_item = None
        max_prob = -1
        # 0正常文本 => code: 0, message: 正常文本
        for k, v in results.items():
            code = re.findall(pattern, k)[0]
            message = k[len(code):]
            cur_prob = float(v)
            item = {
                "riskCode": int(code),
                "riskMessage": message,
                "probability": cur_prob,
            }
            if cur_prob > max_prob:
                max_prob = cur_prob
                max_item = item

            detail.append(item)

        result = {**max_item, 'detail': detail, 'text': input_text}
        return result

    def inference(self, text_list: List[str]):
        ret = []
        for input_text in text_list:
            max_item = self.inference_one(input_text)
            ret.append(max_item)
        return ret

