const scroll = new SmoothScroll('a[href*="#"]', {
  speed: 300,
  offset: 100,
  updateURL: true,
  popstate: true,
  emitEvents: true
})

let beforeTarget
document.addEventListener(
  "scrollStart",
  event => {
    if (beforeTarget !== undefined) {
      beforeTarget.classList.remove("link-target")
    }
    const target = event.detail.anchor
    target.classList.add("link-target")
    beforeTarget = target
  },
  false
)

const termChildren = document.getElementsByClassName("term")[0].children
const header = document.getElementById("header")
const headerChapterInfo = document.getElementById("header_chapter_info")
const headerSectionInfo = document.getElementById("header_section_info")
const headerArticleInfo = document.getElementById("header_article_info")

function regenHeaderInfo() {
  const chapterCurrentlyDisplayedAtTop =
    [...termChildren]
      .filter(
        termChild => termChild.getBoundingClientRect().bottom - header.offsetHeight > 0
      )[0]
  headerChapterInfo.innerHTML =
    document.querySelector(`#${chapterCurrentlyDisplayedAtTop.id} h2`).innerHTML
  headerChapterInfo.style.display = "inline-block"

  const sectionCurrentlyDisplayedAtTop =
    [...document.querySelectorAll(`#${chapterCurrentlyDisplayedAtTop.id} .section`)]
      .filter(
        section => section.getBoundingClientRect().bottom - header.offsetHeight > 0
      )[0]
  if (sectionCurrentlyDisplayedAtTop !== undefined) {
    headerSectionInfo.innerHTML =
      document.querySelector(`#${sectionCurrentlyDisplayedAtTop.id} h3`).innerHTML
    headerSectionInfo.style.display = "inline-block"
  } else {
    headerSectionInfo.style.display = "none"
    headerSectionInfo.innerHTML = ""
  }

  const articleParent =
    sectionCurrentlyDisplayedAtTop !== undefined
      ? sectionCurrentlyDisplayedAtTop
      : chapterCurrentlyDisplayedAtTop
  const articleCurrentlyDisplayedAtTop =
    [...document.querySelectorAll(`#${articleParent.id} .article`)]
      .filter(
        article => article.getBoundingClientRect().bottom - header.offsetHeight > 0
      )[0]
  const articleNumber = document.querySelector(`#${articleCurrentlyDisplayedAtTop.id} h4`).innerHTML
  const articleTitleElement = document.querySelector(`#${articleCurrentlyDisplayedAtTop.id} .article-title`)
  const articleTitle = articleTitleElement != null ? articleTitleElement.innerHTML : ""
  headerArticleInfo.innerHTML = `${articleNumber} ${articleTitle.slice(1, -1)}`
  headerArticleInfo.style.display = "inline-block"
}

regenHeaderInfo()
window.onscroll = regenHeaderInfo
