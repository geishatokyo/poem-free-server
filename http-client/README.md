# HttpClient 

パズエン対戦コードサーバに対してコンソール上でAPIを叩くツールです。

## 使い方

接続テスト
```
Client.ping
```

APIリクエスト
```
Client.post("Path/In/Site", SampleInfoBase(""))
```

POSTデータについては直接stringで入れることも可能です
```
Client.post("Path/In/Site", s"""{ "json" : "data" }""")
```

## 設定変更
アクセス先URLとクライアントバージョンを設定情報として変更できます。

```
Client.baseUrl = "https://target.site.url"
Client.clientVersion = "3.0.1" // フルパッケージ版
```

ローカルテスト用のデフォルト値はソースコードに記載されています。
