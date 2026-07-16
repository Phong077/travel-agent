$ErrorActionPreference = 'Stop'

$proxy = 'http://127.0.0.1:7897'
$root = 'D:\DaSanXia\travel-agent\stitch-export'

$screens = @(
    @{
        dir = '01-day-detail-day1-2'
        title = '每日行程详情 (Day 1-2) (中文)'
        id = 'f752e076a4b84be0b3fe618dfe6668db'
        html = 'https://contribution.usercontent.google.com/download?c=CgthaWRhX2NvZGVmeBJ7Eh1hcHBfY29tcGFuaW9uX2dlbmVyYXRlZF9maWxlcxpaCiVodG1sXzAwMDY1NmE2ZDNlNzdiNzUwNjM5NzE2NThiMmQ3ZGZiEgsSBxDz15XpgAkYAZIBIwoKcHJvamVjdF9pZBIVQhM5Nzg2NjAzMzQxMTgzNTUzODAw&filename=&opi=89354086'
        image = 'https://lh3.googleusercontent.com/aida/AP1WRLvjQLs48dLTEmdbpEhaW2eer2F8w-3QgzsLDwMaWgEr9hP3W3VXxut1tw0utHT712g9cA8Iufa3L0_M6shhvFT6MQuXDDglplSQbUedLBZgrhGKXi7EOHWhJygXJXwJj6nDBIZq3l_k5X0exjbnKgFEYCRBZkgaUOFBXj7XmSshVrcPPq4UcTvUy4SHKvZErpeVy0-6kEMw2If2Oc97zi0L4cUtiXf6q2beJLkRvPjCAAasleAXi1y6p7U'
    }
    @{
        dir = '02-dashboard-home'
        title = '控制台首页 (中文)'
        id = '06cf26b2cc8a4e0f80497da3aa52e377'
        html = 'https://contribution.usercontent.google.com/download?c=CgthaWRhX2NvZGVmeBJ7Eh1hcHBfY29tcGFuaW9uX2dlbmVyYXRlZF9maWxlcxpaCiVodG1sXzAwMDY1NmE2ZDQ5ODU0YjkwMzgzOTJmYjc4MzM4MTc1EgsSBxDz15XpgAkYAZIBIwoKcHJvamVjdF9pZBIVQhM5Nzg2NjAzMzQxMTgzNTUzODAw&filename=&opi=89354086'
        image = 'https://lh3.googleusercontent.com/aida/AP1WRLu-WoR-OXasYrrns0YPxM0OfBcl-YegUsOhFKRVN-MtwmWA2cQCLgrCRujxoVI_N4FCJaPelixWTY8PKb2_2muJfDqjxvGjop6HmABdo0ZKXzNwVA1ohyadqq8LbUDRKvYCa3KYH_HxHXN7q-L7H7cTYtNxYsn5QQ5O5SCF7c8Ce73uwLsglZkBVbHw_xDA2Ujqog7EWP-blukgimZ0-U8hNVMj_E2DAvA7c5ig7n44ypCfCOReZn89FTY'
    }
    @{
        dir = '03-knowledge-search-debug'
        title = '知识库检索调试页 (中文)'
        id = '680335b438f84d35b8c58b8736dfc298'
        html = 'https://contribution.usercontent.google.com/download?c=CgthaWRhX2NvZGVmeBJ7Eh1hcHBfY29tcGFuaW9uX2dlbmVyYXRlZF9maWxlcxpaCiVodG1sXzAwMDY1NmE2ZDRlYTFmMjMwOTI1ZDM1OWVkMmJhZDFhEgsSBxDz15XpgAkYAZIBIwoKcHJvamVjdF9pZBIVQhM5Nzg2NjAzMzQxMTgzNTUzODAw&filename=&opi=89354086'
        image = 'https://lh3.googleusercontent.com/aida/AP1WRLtqGgzzkxZVX4sA2YqBLjGeKvJ1l8pjHpkKGCWn3njoYOLJAnu1iRbuUEzzVI-hoEeekOgI-HcN7xghvSH-2WZeuwCwlPMrlpvhYU-Wwq6EOJZolTPAP2nyDjWYtapp2dDZyaLMol6dnAp3ZNRiAXS8F4FIwrG-dFKSwJ-gpFzID-s3ZbbZnDpD8nKnPbTnVwIynAOfaJlbVvtMXUCC38Eh3Ma5HfIl02YuLm3c9itTGt57R3Cu_L6KOGs'
    }
    @{
        dir = '04-generating-loading'
        title = '生成中加载状态 (中文)'
        id = '8e4b05e8720b480aa3bf34c0e1c4c82d'
        html = 'https://contribution.usercontent.google.com/download?c=CgthaWRhX2NvZGVmeBJ7Eh1hcHBfY29tcGFuaW9uX2dlbmVyYXRlZF9maWxlcxpaCiVodG1sXzAwMDY1NmE2ZDQwN2QzOWMwNzc5YmE5ZjI4MWUwZjlkEgsSBxDz15XpgAkYAZIBIwoKcHJvamVjdF9pZBIVQhM5Nzg2NjAzMzQxMTgzNTUzODAw&filename=&opi=89354086'
        image = 'https://lh3.googleusercontent.com/aida/AP1WRLvhqkJetxCrLxrVQcluTbMECs0saj_sFu1iTr4--8EHX9L9BjtpMw5qMIA0KrVgAkv6LdOnEfygqlESohm7WanaUpt73BTDWWvHTtYgif3Ipta39GtouDsIHLuMkmiEsAS00emtk9qwqmECzfCLJMdOC09phUALm4WRx4pXmfPf8AbH3lvR88Z-tAE5kmyP89xNLbh8UDIHXYQSNoSq1pDt6IsoFs4Eg4Xtwe7fnApw8TDmvj5a5iBCqIc'
    }
    @{
        dir = '05-rag-references'
        title = '知识库引用来源 (RAG References) (中文)'
        id = 'd86745ada9fd4aa3b29b94e9173594e3'
        html = 'https://contribution.usercontent.google.com/download?c=CgthaWRhX2NvZGVmeBJ7Eh1hcHBfY29tcGFuaW9uX2dlbmVyYXRlZF9maWxlcxpaCiVodG1sXzAwMDY1NmE2ZDQ0NmZlNTQwODI5ODlmNjkyMDdlY2NjEgsSBxDz15XpgAkYAZIBIwoKcHJvamVjdF9pZBIVQhM5Nzg2NjAzMzQxMTgzNTUzODAw&filename=&opi=89354086'
        image = 'https://lh3.googleusercontent.com/aida/AP1WRLsKU_Ols0c3UBBdyAzWFCKI0C8HbbcjpGH4tYaIdAbrBoNRaJ-nxsYv9vFbis8rfKo4yUwvpfAHL41EZOI-5eDNitWfKRgm-I0sgTyp-Ly8YJuQ1a5LplzXyoXstNm7DLhrPoohdJZqOxdv5KANDs2l5QxuKReEFQFDjg2mCHDrIFt4sENbRegJMN9gzPwKmREQUSerQqS4xfQPiQ0yhkE7NLYpZax1n81lUxoAwPm3Kvat6-HPBTUAug'
    }
    @{
        dir = '06-day-detail-day3-5'
        title = '每日行程详情 (Day 3-5) (中文)'
        id = '79ac0565facd4923b0886ae314d5d2d7'
        html = 'https://contribution.usercontent.google.com/download?c=CgthaWRhX2NvZGVmeBJ7Eh1hcHBfY29tcGFuaW9uX2dlbmVyYXRlZF9maWxlcxpaCiVodG1sXzAwMDY1NmE2ZDVjNGIxNmUwODlhZjVlN2NiMjcxZmZlEgsSBxDz15XpgAkYAZIBIwoKcHJvamVjdF9pZBIVQhM5Nzg2NjAzMzQxMTgzNTUzODAw&filename=&opi=89354086'
        image = 'https://lh3.googleusercontent.com/aida/AP1WRLvbLSroEC24msXKRTPGhxmIdimnum4nOBm5tQ-tnN3xSAlO7VMT6yz6Fr6k-o6uRmSd66nHLUBQTJZy-kBN0NebOgyr6L4lGOJ36ixJzv1W5_ZtlUlbZY4qOihhHlv2XdxLNbfq48RSKwEl7Z5r_yeONm4-stPjDWq4CATxWqLx4NHJblrKbqIy1uhpOHOIzFaK9NwigAIs2EGpRt3DVtj25BWw17bJQ_GUFi2aNRmvUAu4ZZZEXtR2wfg'
    }
    @{
        dir = '07-trip-input-form'
        title = '旅行规划输入表单 (中文)'
        id = 'a336d0f0f3be40c7a3cbb6f7c965eb0c'
        html = 'https://contribution.usercontent.google.com/download?c=CgthaWRhX2NvZGVmeBJ7Eh1hcHBfY29tcGFuaW9uX2dlbmVyYXRlZF9maWxlcxpaCiVodG1sXzAwMDY1NmE2ZDUzOTIwYzgwMzMyY2ExOWNhMTgzZGRiEgsSBxDz15XpgAkYAZIBIwoKcHJvamVjdF9pZBIVQhM5Nzg2NjAzMzQxMTgzNTUzODAw&filename=&opi=89354086'
        image = 'https://lh3.googleusercontent.com/aida/AP1WRLuWaTmbRubksX_VaFP5NeGv08At5zbDtNK__zgI-uK0_q2Y8gLSy4MG_6UyzB5ErYmJjPX06rzHOSnhETCNyoMGZ2nBm5ZC-NpVqQaDuvU66ifGLDDXz_TDpBtdDHlT_dK_GO4eJ565hIqRXbKHZxdUJ2XR9S6UFTbIKO1QshYCl6zPYh010P5hrxgWz0b7b8848pApjIijGu9gKd6i9zo5UL5Mk1lMi909qO2VjDg5SKHJ_YUtjcW9Xfs'
    }
    @{
        dir = '08-mobile-input'
        title = '移动端：需求输入页 (中文)'
        id = 'a8800c5da78d4e44aea928fc1ba107c5'
        html = 'https://contribution.usercontent.google.com/download?c=CgthaWRhX2NvZGVmeBJ7Eh1hcHBfY29tcGFuaW9uX2dlbmVyYXRlZF9maWxlcxpaCiVodG1sXzAwMDY1NmE2ZDQ4N2E3ZDIwMzgzYTFmNmE0MmJlYjU0EgsSBxDz15XpgAkYAZIBIwoKcHJvamVjdF9pZBIVQhM5Nzg2NjAzMzQxMTgzNTUzODAw&filename=&opi=89354086'
        image = 'https://lh3.googleusercontent.com/aida/AP1WRLsKjWUIpHLX5lu8tqxpd1zkHXU2ERMQyHNo_k8mwBgcAV0xTJWfTH0VtRvc5Pg8UdnfUjF6FqSQHN1aSNnwc_AoNqgQ9LZxUSmcmNmaLLGtc2ao6cC2xfwVT20cy7xfzBJ0u4Zjce8U7rccTDTrD0obLM7880oP7khtZU8mn7_trx25MemkIsVISVVscFNB30_KwCaZaIX1D5Unj5_GtYVok9u0axK-P-nmEXEc8FlBDecJ8_7TMOpGSvM'
    }
    @{
        dir = '09-mobile-itinerary'
        title = '移动端：行程详情页 (中文)'
        id = 'fd8fbbee09714312bca98f49f570fd8d'
        html = 'https://contribution.usercontent.google.com/download?c=CgthaWRhX2NvZGVmeBJ7Eh1hcHBfY29tcGFuaW9uX2dlbmVyYXRlZF9maWxlcxpaCiVodG1sXzAwMDY1NmE2ZDRhZGJlYjYwMmQzYzI4YjNkMGVlOTlmEgsSBxDz15XpgAkYAZIBIwoKcHJvamVjdF9pZBIVQhM5Nzg2NjAzMzQxMTgzNTUzODAw&filename=&opi=89354086'
        image = 'https://lh3.googleusercontent.com/aida/AP1WRLvGIbDWEmGCY-Tg90hPFKStX0PiZBPAPGdD4OKzG9ioNnEDN7blXycBHythRfPA9tSR1Uy-XoqmM_1IXbqGtZCBtK9AGP5ZJu_kqoJy78O5n2wEUr_zO6NOrzm1Z8nmxhBaayEBVTM4omdZhfgz1Krl3ndPB0LN-JZvu9vaNXwOzHle-onZUnHT3j5D6znwgGErp3e3THfCeP7bA2h6c3rjU9kvYCYQp8jg-P5s0SjZYb2Ldue1RZvqTO0'
    }
    @{
        dir = '10-overview-budget'
        title = '旅行计划概览及预算分析 (中文)'
        id = '002c9a528e1047aebc6ea2f47bb79700'
        html = 'https://contribution.usercontent.google.com/download?c=CgthaWRhX2NvZGVmeBJ7Eh1hcHBfY29tcGFuaW9uX2dlbmVyYXRlZF9maWxlcxpaCiVodG1sXzAwMDY1NmE2ZDUyYTcyOWYwNDMxMTlmODg1MWQyZTAyEgsSBxDz15XpgAkYAZIBIwoKcHJvamVjdF9pZBIVQhM5Nzg2NjAzMzQxMTgzNTUzODAw&filename=&opi=89354086'
        image = 'https://lh3.googleusercontent.com/aida/AP1WRLtfUPV8nfVepbR2lQAiuE3yXr7aX2MyEWxrVUNJVLfW7SgzhkkeKyWd-iYEU2itCruxhONqE67EFSViK8suEsHW9dTEfuREvs94Akj_Djj6UWhT83Mow794SiaElIMLdz8PxySvmJMHeV4BwqFZCgOMYxRCXt1scF7qbDlgjm44H1_QGvF7m_R9oOCpQjt1PBBUAh5ycDD6ynEWAgSIr-Unxbzozro6xqDhq2dsBU0QtVLEg5W7x2p6sT8'
    }
)

New-Item -ItemType Directory -Force -Path $root | Out-Null

foreach ($screen in $screens) {
    $targetDir = Join-Path $root $screen.dir
    New-Item -ItemType Directory -Force -Path $targetDir | Out-Null

    $metadata = @{
        title = $screen.title
        id = $screen.id
    } | ConvertTo-Json -Depth 4
    Set-Content -Path (Join-Path $targetDir 'metadata.json') -Value $metadata -Encoding UTF8

    Write-Host "Downloading $($screen.dir) code.html"
    curl.exe -L --fail --retry 2 --proxy $proxy --connect-timeout 20 --max-time 180 -o (Join-Path $targetDir 'code.html') $screen.html

    Write-Host "Downloading $($screen.dir) screenshot.png"
    curl.exe -L --fail --retry 2 --proxy $proxy --connect-timeout 20 --max-time 180 -o (Join-Path $targetDir 'screenshot.png') $screen.image
}

Write-Host "Downloaded Stitch prototype screens to $root"
