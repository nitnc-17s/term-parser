# term-parser
規約をパースするよ

## 使い方
```
term-parser 1.1.0 by LaFr4nc3
Usage: term-parser [options] [<source>] [<target>]

  -v, --version
  -h, --help             このヘルプを表示
  -S, --use-std          標準入出力を使用する
  -t, --title <value>    パース結果のHTMLで利用するタイトル
  --indent-size <value>  リストのパース時に使うインデントのサイズ (default: 4)
  <source>               パース対象のファイル (default: terms.txt)
  <target>               パース結果の出力ファイル (default: terms.html)
```

標準入力および標準出力に対応しています。
一部1行で出力されるので、別途整形ツールなどをお使いください。

不明な点がありましたら、LaFr4nc3までご連絡ください。

## 使用例
`term-parser -t "学生会規約改正案 (2019年度発議)" examples/terms.txt examples/terms.html`

例のファイルが`examples`にあります。

## 文法

### 章: Chapter
```
第${n}章 ${title}
${contents}
```
- `${n}`: 章番号 (数字のみ)
- `${title}`: 章のタイトル
- `${contents}`: 章の内容 (type: List[Section] | List[Article])

### 附則: SupplementaryProvisions
```
附則
${contents}
```
- `${contents}`: 附則の内容 (type: List[Article])

### 節: Section
章に属している必要があります。

```
第${n}節 ${title}
${contents}
```
- `${n}`: 節番号 (数字のみ)
- `${title}`: 節のタイトル
- `${contents}`: 節の内容 (List[Article])

### 条: Article
章、附則または節に属している必要があります。

#### 条のタイトルがある場合
```
(${title})
第${n}条
${contents}
```
- `${title}`: 条のタイトル
- `${n}`: 条番号 (数字のみ)
- `${contents}`: 条の内容 (type: List[Paragraph])

#### 条のタイトルがない場合
```
第${n}条
${contents}
```
- `${n}`: 条番号 (数字のみ)
- `${contents}`: 条の内容 (type: List[Paragraph])

### 項: Paragraph
条に属している必要があります。

#### 第一項の場合
```
${contents}

```
- `${contents}`: 項の内容 (type: List[ParagraphText | OrderedList])

#### 第二項以降の場合
```
${n}
${contents}

```
- `${n}`: 項番号
- `${contents}`: 項の内容 (type: List[ParagraphText | OrderedList])

### 項の文章: ParagraphText
項に属してる必要があります。

```
${contents}
```
- `${contents}`: 項の文章の内容 (type: List[Text])

### テキスト: Text
項の文章かリストのテキストに属している必要があります。

#### 普通のテキスト: PlainText
普通の文字列です。

#### リンクテキスト: LinkText

##### 別条の項の列挙
```
第${a}条第${p1}項<${sep}第${pn}項>
```
- `${a}`: リンク先の条番号
- `${p1}`: リンク先の項番号
- `<...>`: 繰り返し
    - `${sep}`: 区切り文字 (`、`, `及び`)
    - `${pn}`: リンク先の項番号

##### 別条の項
```
第${a}条第${p}項
```
- `${a}`: リンク先の条番号
- `${p}`: リンク先の項番号

##### 同条の項
```
第${p}項
```
- `${p}`: リンク先の項番号

##### 同条の前項
```
前項
```

### リスト: OrderedList
項かリストのアイテムに属している必要があります。
```
${contents}
```
- `${contents}`: リストの内容 (type: List[OrderedListItem])

### リストのアイテム: OrderedListItem
リストに属している必要があります。
```
${indent}${n} ${contents}
```
- `${indent}`: インデント (default: 4)
- `${n}`: アイテムの番号 深さによって使う文字種が異なる (depth: {1: "漢数字", 2: "イロハ"})
- `${contents}`: アイテムの内容 (type: List[OrderedList | Text])
