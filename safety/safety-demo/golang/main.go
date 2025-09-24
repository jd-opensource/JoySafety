package main

import (
	"bytes"
	"crypto/hmac"
	"crypto/sha1"
	"encoding/base64"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"strings"
	"time"

	"github.com/google/uuid"
)

const (
	AK = "YOUR_AK"
	SK = "YOUR_SK"
)

const (
	LLM_SEC_DETECT_BASE_URL = "http://safety-api:8007/llmsec/api/defense/v2"

	BUSINESS_TYPE_TO_B = "toB"

	RESPONSE_MODE_SYNC      = "sync"
	RESPONSE_MODE_FREE_TAXI = "free_taxi"

	CONTENT_TYPE_TEXT = "text"

	ROLE_ROBOT = "robot"
	ROLE_USER  = "user"

	ACCESS_TARGET_DEFENSE_V2 = "defenseV2"

	PARAM_KEY_ACCESS_KEY    = "accessKey"
	PARAM_KEY_ACCESS_TARGET = "accessTarget"
	PARAM_KEY_REQUEST_ID    = "requestId"
	PARAM_KEY_TIMESTAMP     = "timestamp"
	PARAM_KEY_PLAIN_TEXT    = "plainText"

	MESSAGE_TYPE_INPUT  = "input"
	MESSAGE_TYPE_OUTPUT = "output"
)

type llmSecDetectReq struct {
	RequestID    string      `json:"requestId"`
	Timestamp    int64       `json:"timestamp"`
	AccessKey    string      `json:"accessKey"`
	PlainText    string      `json:"plainText"`
	Signature    string      `json:"signature"`
	BusinessType string      `json:"businessType"`
	ResponseMode string      `json:"responseMode"`
	ContentType  string      `json:"contentType"`
	Content      string      `json:"content"`
	MessageInfo  messageInfo `json:"messageInfo"`
}

type messageInfo struct {
	SessionID string                 `json:"sessionId"`
	MessageID int                    `json:"messageId"`
	SliceID   int                    `json:"sliceId"`
	FromRole  string                 `json:"fromRole"`
	FromID    string                 `json:"fromId"`
	ToRole    string                 `json:"toRole"`
	ToID      string                 `json:"toId"`
	Ext       map[string]interface{} `json:"ext"`
}

type llmSecDetectResp struct {
	Code    int    `json:"code"`
	Message string `json:"message"`
	Cost    int    `json:"cost"`
	Data    []struct {
		Requests []struct {
			SessionID string                 `json:"sessionId"`
			MessageID int                    `json:"messageId"`
			SliceID   int                    `json:"sliceId"`
			FromRole  string                 `json:"fromRole"`
			FromID    string                 `json:"fromId"`
			ToRole    string                 `json:"toRole"`
			ToID      string                 `json:"toId"`
			Ext       map[string]interface{} `json:"ext"`
		} `json:"requests"`
		CheckedContent  string `json:"checkedContent"`
		RiskCode        int    `json:"riskCode"`
		RiskMessage     string `json:"riskMessage"`
		RiskCheckType   string `json:"riskCheckType"`
		RiskCheckName   string `json:"riskCheckName"`
		RiskCheckResult struct {
			Type           string  `json:"type"`
			RiskCode       int     `json:"riskCode"`
			RiskMessage    string  `json:"riskMessage"`
			SrcName        string  `json:"srcName"`
			CheckedContent string  `json:"checkedContent"`
			Cost           int     `json:"cost"`
			Probability    float32 `json:"probability"`
			Detail         []struct {
				RiskCode    int     `json:"riskCode"`
				RiskMessage string  `json:"riskMessage"`
				Probability float64 `json:"probability"`
			} `json:"detail"`
		} `json:"riskCheckResult"`
	} `json:"data"`
}

func main() {
	result, err := LLMSecDetect(MESSAGE_TYPE_INPUT, "测试文本")
	if err != nil {
		panic(err)
	}
	fmt.Printf("%+v", result)
}

func LLMSecDetect(messageType string, text string) (*llmSecDetectResp, error) {
	req := llmSecDetectReq{
		RequestID:    uuid.New().String(),
		Timestamp:    time.Now().UnixMilli(),
		AccessKey:    AK,
		BusinessType: BUSINESS_TYPE_TO_B,
		ResponseMode: RESPONSE_MODE_SYNC,
		ContentType:  CONTENT_TYPE_TEXT,
		Content:      text,
		PlainText:    text,
		MessageInfo:  messageInfo{},
	}
	if messageType == MESSAGE_TYPE_INPUT {
		req.MessageInfo.FromRole = ROLE_USER
		req.MessageInfo.ToRole = ROLE_ROBOT
	} else {
		req.MessageInfo.FromRole = ROLE_ROBOT
		req.MessageInfo.ToRole = ROLE_USER
	}
	req.Signature = genSignature(req)

	data, err := json.Marshal(req)
	if err != nil {
		return nil, err
	}

	url := fmt.Sprintf(
		"%s/%s",
		LLM_SEC_DETECT_BASE_URL,
		AK)
	r, err := http.Post(url, "application/json", bytes.NewBuffer(data))
	if err != nil {
		return nil, err
	}
	defer r.Body.Close()

	var resp llmSecDetectResp
	body, err := io.ReadAll(r.Body)
	if err != nil {
		return nil, err
	}
	if err := json.Unmarshal(body, &resp); err != nil {
		return nil, err
	}
	if resp.Code != 0 {
		return nil, errors.New(resp.Message)
	}

	return &resp, nil
}

func genSignature(req llmSecDetectReq) string {
	sequence := []string{
		PARAM_KEY_ACCESS_KEY, PARAM_KEY_ACCESS_TARGET, PARAM_KEY_REQUEST_ID,
		PARAM_KEY_TIMESTAMP, PARAM_KEY_PLAIN_TEXT}
	m := map[string]string{
		PARAM_KEY_ACCESS_KEY:    req.AccessKey,
		PARAM_KEY_ACCESS_TARGET: ACCESS_TARGET_DEFENSE_V2,
		PARAM_KEY_REQUEST_ID:    req.RequestID,
		PARAM_KEY_TIMESTAMP:     fmt.Sprintf("%d", req.Timestamp),
		PARAM_KEY_PLAIN_TEXT:    req.PlainText,
	}
	var params []string
	for _, key := range sequence {
		params = append(params, fmt.Sprintf("%s=%s", key, m[key]))
	}
	mac := hmac.New(sha1.New, []byte(SK))
	mac.Write([]byte(strings.Join(params, "&")))
	return base64.StdEncoding.EncodeToString(mac.Sum(nil))
}
