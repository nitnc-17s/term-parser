const scroll = new SmoothScroll('a[href*="#"]', {
  speed: 300,
  offset: 100,
  updateURL: true,
  popstate: true,
  emitEvents: true
})

let prevTarget
document.addEventListener(
  "scrollStart",
  event => {
    if (prevTarget !== undefined) {
      prevTarget.classList.remove("link-target")
    }
    const target = event.detail.anchor
    target.classList.add("link-target")
    prevTarget = target
  },
  false
)

const termChildren = document.getElementsByClassName("term")[0].children
const header = document.getElementById("header")
const headerChapterInfo = document.getElementById("header_chapter_info")
const headerSectionInfo = document.getElementById("header_section_info")
const headerArticleInfo = document.getElementById("header_article_info")

let prevChapter, prevSection, prevArticle
function regenHeaderInfo() {
  const chapterCurrentlyDisplayedAtTop =
    [...termChildren]
      .filter(
        termChild => termChild.getBoundingClientRect().bottom - header.offsetHeight > 0
      )[0]
  if (chapterCurrentlyDisplayedAtTop !== prevChapter) {
    const chapterAnchor = document.createElement("a")
    chapterAnchor.className = "header-anchor"
    chapterAnchor.href = `#${chapterCurrentlyDisplayedAtTop.id}`
    chapterAnchor.setAttribute("data-scroll", "")
    chapterAnchor.innerHTML =
      document.querySelector(`#${chapterCurrentlyDisplayedAtTop.id} h2`).innerHTML
    while (headerChapterInfo.firstChild) {
      headerChapterInfo.removeChild(headerChapterInfo.firstChild)
    }
    headerChapterInfo.appendChild(chapterAnchor)
    headerChapterInfo.style.display = "inline-block"
    prevChapter = chapterCurrentlyDisplayedAtTop
  }

  const sectionCurrentlyDisplayedAtTop =
    [...document.querySelectorAll(`#${chapterCurrentlyDisplayedAtTop.id} .section`)]
      .filter(
        section => section.getBoundingClientRect().bottom - header.offsetHeight > 0
      )[0]
  if (sectionCurrentlyDisplayedAtTop !== prevSection) {
    if (sectionCurrentlyDisplayedAtTop !== undefined) {
      const sectionAnchor = document.createElement("a")
      sectionAnchor.className = "header-anchor"
      sectionAnchor.href = `#${sectionCurrentlyDisplayedAtTop.id}`
      sectionAnchor.setAttribute("data-scroll", "")
      sectionAnchor.innerHTML =
        document.querySelector(`#${sectionCurrentlyDisplayedAtTop.id} h3`).innerHTML
      while (headerSectionInfo.firstChild) {
        headerSectionInfo.removeChild(headerSectionInfo.firstChild)
      }
      headerSectionInfo.appendChild(sectionAnchor)
      headerSectionInfo.style.display = "inline-block"
    } else {
      headerSectionInfo.style.display = "none"
      headerSectionInfo.innerHTML = ""
    }
    prevSection = sectionCurrentlyDisplayedAtTop
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
  if (articleCurrentlyDisplayedAtTop !== prevArticle) {
    const articleNumber = document.querySelector(`#${articleCurrentlyDisplayedAtTop.id} h4`).innerHTML
    const articleTitleElement = document.querySelector(`#${articleCurrentlyDisplayedAtTop.id} .article-title`)
    const articleTitle = articleTitleElement != null ? articleTitleElement.innerHTML : ""
    const articleAnchor = document.createElement("a")
    articleAnchor.className = "header-anchor"
    articleAnchor.href = `#${articleCurrentlyDisplayedAtTop.id}`
    articleAnchor.setAttribute("data-scroll", "")
    articleAnchor.innerHTML = `${articleNumber} ${articleTitle.slice(1, -1)}`
    while (headerArticleInfo.firstChild) {
      headerArticleInfo.removeChild(headerArticleInfo.firstChild)
    }
    headerArticleInfo.appendChild(articleAnchor)
    headerArticleInfo.style.display = "inline-block"
    prevArticle = articleCurrentlyDisplayedAtTop
  }
}

regenHeaderInfo()
window.onscroll = regenHeaderInfo
