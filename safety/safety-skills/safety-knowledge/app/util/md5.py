import hashlib


def get_md5(text: str):
    # 创建md5对象
    md5 = hashlib.md5()
    # 更新md5对象，这里需要将字符串转换为字节
    md5.update(text.encode('utf-8'))
    # 获取16进制格式的散列值
    return md5.hexdigest()


if __name__ == '__main__':
    # 使用示例
    text = 'Hello, World!'
    md5_result = get_md5(text)
    print(md5_result)  # 输出：65a8e27d8879283831b664bd8b7f0ad4